package org.kin.framework.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 与普通executor最大的区别是该实现并不会管理任何线程
 * 使用场景: 相当于等待一个callback task. 当caller thread等待future完成时, 通过{@link #execute(Runnable)}执行的task都会进入queue缓存, 当future完成了, queue里面的task会在caller thread中执行
 *
 * Forked from dubbo <A>https://github.com/apache/dubbo<A/>.
 * @author huangjianqin
 * @date 2022/5/20
 */
public final class ThreadLessExecutor{
    private static final Logger log = LoggerFactory.getLogger(ThreadLessExecutor.class);

    /** task queue */
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    /** waiting future */
    private final CompletableFuture<?> waitingFuture;
    /** internal lock */
    private final Object lock = new Object();
    /** executor是否已接受callback task(即future完成)并执行完{@link #queue}中缓存的task */
    private volatile boolean finished = false;
    /** 是否正在等待callback task */
    private volatile boolean waiting = true;
    /** 等待callback task的线程 */
    private Thread waitingThread;

    public ThreadLessExecutor(@Nonnull CompletableFuture<?> waitingFuture) {
        this.waitingFuture = waitingFuture;
    }

    /**
     * 等待直到有task进入queue, 然后所有queue缓存的task都会被执行
     * 方法仅仅允许调用一次
     */
    public void waitAndDrain() throws InterruptedException {
        if (isFinished()) {
            return;
        }

        //虽然waitAndDrain()并不会存在多线程问题, 但是queue里面的task的执行顺序就无法保证
        //所以此处限制仅仅允许一条线程来等待callback task
        synchronized (lock) {
            if(Objects.isNull(waitingThread)){
                waitingThread = Thread.currentThread();
            }
            else{
                throw new IllegalStateException("just allow one thread to wait and drain");
            }
        }

        Runnable runnable;
        try {
            //等待callback task
            runnable = queue.take();
        } catch (InterruptedException e) {
            setWaiting(false);
            throw e;
        }

        synchronized (lock) {
            //解除waiting状态
            setWaiting(false);
            //执行callback task
            runnable.run();
        }

        //执行队列中的剩余task
        runnable = queue.poll();
        while (runnable != null) {
            runnable.run();
            runnable = queue.poll();
        }

        //mark finished
        setFinished(true);
    }

    /**
     * future finish and then execute callback {@code runnable}
     * @param runnable  callback
     */
    public void execute(@Nonnull Runnable runnable) {
        runnable = new RunnableWrapper(runnable);

        synchronized (lock) {
            if (!isWaiting()) {
                //非waiting状态, 直接执行
                runnable.run();
            }
            else{
                //等待callback task后再执行
                queue.add(runnable);
            }
        }
    }

    /**
     * 异常callback, 终止{@link #waitAndDrain()}的阻塞并唤醒, 避免无休止的等待
     */
    public void notifyExceptionally(Throwable t) {
        execute(() -> {
            if (!waitingFuture.isDone()) {
                waitingFuture.completeExceptionally(t);
            }
        });
    }

    //setter && getter
    private boolean isFinished() {
        return finished;
    }

    private void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isWaiting() {
        return waiting;
    }

    private void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    /**
     * {@link Runnable}的简单封装, 当{@link Runnable#run()}执行异常时会log打印异常
     */
    private static class RunnableWrapper implements Runnable {
        private final Runnable runnable;

        public RunnableWrapper(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } catch (Throwable t) {
                log.error("", t);
            }
        }
    }
}
