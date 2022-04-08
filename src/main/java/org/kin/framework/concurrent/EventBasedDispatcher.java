package org.kin.framework.concurrent;

import org.jctools.maps.NonBlockingHashMap;
import org.jctools.maps.NonBlockingHashSet;
import org.kin.framework.utils.SysUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 底层消息处理实现是基于event loop
 * 消息有序处理, 但不保证在同一线程下执行, 不要使用{@link ThreadLocal}
 * 尽量不要blocking
 *
 * @author huangjianqin
 * @date 2020-04-15
 */
public final class EventBasedDispatcher<KEY, MSG> extends AbstractDispatcher<KEY, MSG> {
    private static final Logger log = LoggerFactory.getLogger(EventBasedDispatcher.class);
    /** '毒药', 用于终止message loop */
    private final InBox<MSG> POISON_PILL = new InBox<MSG>(null, false);

    /** 并发数, 即event loop数量 */
    private final int parallelism;
    /** 注册的{@link Receiver} */
    private final Map<KEY, InBox<MSG>> inBoxMap = new NonBlockingHashMap<>();
    /** 需要处理消息的{@link Receiver} */
    private final LinkedBlockingQueue<InBox<MSG>> pendingDataQueue = new LinkedBlockingQueue<>();
    /** 标识, 如果{@link InBox}正在被处理, 则不需要入队, 减少队列长度 */
    private final NonBlockingHashSet<InBox<MSG>> runningInboxes = new NonBlockingHashSet<>();
    /** 是否已启动message loop */
    private volatile boolean isMessageLoopRunning;

    public EventBasedDispatcher(int parallelism) {
        super(ExecutionContext.forkjoin(
                parallelism, "eventBasedDispatcher",
                SysUtils.CPU_NUM / 2 + 1));
        this.parallelism = parallelism;
    }

    @Override
    public void register(KEY key, Receiver<MSG> receiver, boolean enableConcurrent) {
        if (isStopped()) {
            throw new IllegalStateException("dispatcher is closed");
        }

        if (Objects.isNull(key)) {
            throw new IllegalArgumentException("key is null");
        }

        if (Objects.isNull(receiver)) {
            throw new IllegalArgumentException("receiver is null");
        }

        if (Objects.nonNull(inBoxMap.putIfAbsent(key, new InBox<>(receiver, enableConcurrent)))) {
            throw new IllegalArgumentException(String.format("receiver with key `%s` has registered", key));
        }

        InBox<MSG> inBox = inBoxMap.get(key);
        pendingDataQueue.offer(inBox);
        runningInboxes.add(inBox);
        runMessageLoop();
    }

    /**
     * 初始化并启动message loop
     */
    private void runMessageLoop(){
        if (!isMessageLoopRunning) {
            synchronized (this) {
                if (!isMessageLoopRunning) {
                    for (int i = 0; i < parallelism; i++) {
                        executionContext.execute(new MessageLoop());
                    }
                    isMessageLoopRunning = true;
                }
            }
        }
    }

    @Override
    public void unregister(KEY key) {
        if (isStopped()) {
            return;
        }

        unregister0(key);
    }

    private void unregister0(KEY key){
        if (Objects.isNull(key)) {
            throw new IllegalArgumentException("key is null");
        }

        InBox<MSG> inBox = inBoxMap.remove(key);
        if (Objects.nonNull(inBox)) {
            inBox.close();
            if(runningInboxes.add(inBox)){
                pendingDataQueue.offer(inBox);
            }
        }
    }


    @Override
    public boolean isRegistered(KEY key) {
        if (isStopped()) {
            return false;
        }

        return inBoxMap.containsKey(key);
    }

    @Override
    public void postMessage(KEY key, MSG message) {
        if (isStopped()) {
            throw new IllegalStateException("dispatcher is closed");
        }

        if (Objects.isNull(key)) {
            throw new IllegalArgumentException("key is null");
        }

        if (Objects.isNull(message)) {
            throw new IllegalArgumentException("message is null");
        }

        InBox<MSG> inBox = inBoxMap.get(key);
        if (Objects.nonNull(inBox)) {
            inBox.post(new InBox.ReceiverMessage<>(message));
            if(runningInboxes.add(inBox)){
                pendingDataQueue.offer(inBox);
            }
        }
    }

    @Override
    public void post2All(MSG message) {
        if (isStopped()) {
            throw new IllegalStateException("dispatcher is closed");
        }

        if (Objects.isNull(message)) {
            throw new IllegalArgumentException("message is null");
        }

        for (KEY key : inBoxMap.keySet()) {
            postMessage(key, message);
        }
    }

    @Override
    protected void doClose() {
        inBoxMap.keySet().forEach(this::unregister0);
        for (int i = 0; i < parallelism; i++) {
            //terminate n个message loop
            pendingDataQueue.offer(POISON_PILL);
        }

        //help gc
        inBoxMap.clear();
    }

    //------------------------------------------------------------------------------------------------------------------------
    /**
     * 消息处理loop
     */
    private class MessageLoop implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    InBox<MSG> inBox = pendingDataQueue.take();
                    if (inBox == POISON_PILL) {
                        //terminated
                        return;
                    }
                    //防止与post操作线程安全问题, 先移除running标识, 最多多入队了一个inbox
                    runningInboxes.remove(inBox);
                    //消息处理
                    inBox.process();
                }
            } catch (InterruptedException e) {
                //do nothing
            } catch (Exception e) {
                log.error("", e);
                //re-run
                executionContext.execute(new MessageLoop());
            }
        }
    }
}
