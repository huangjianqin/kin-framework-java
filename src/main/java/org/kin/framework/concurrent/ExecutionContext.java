package org.kin.framework.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author huangjianqin
 * @date 2018/1/24
 */
public class ExecutionContext implements ScheduledExecutorService {
    /** 默认scheduler后缀, scheduler name = worker name + {@link #DEFAULT_SCHEDULER_NAME} */
    public static final String DEFAULT_SCHEDULER_NAME = "-scheduler";
    /** 工作线程 */
    private final ExecutorService worker;
    /** 调度线程 */
    private ScheduledExecutorService scheduler;
    private volatile boolean isStopped;

    public ExecutionContext(ExecutorService worker) {
        this.worker = worker;
    }

    public ExecutionContext(ExecutorService worker, int scheduleParallelism) {
        this(worker, scheduleParallelism, new SimpleThreadFactory("executionContext".concat(DEFAULT_SCHEDULER_NAME)));
    }

    public ExecutionContext(ExecutorService worker, int scheduleParallelism, String schedulerPrefix) {
        this(worker, scheduleParallelism, new SimpleThreadFactory(schedulerPrefix));
    }

    public ExecutionContext(ExecutorService worker, int scheduleParallelism, ThreadFactory schedulerFactory) {
        Preconditions.checkArgument(scheduleParallelism > 0, "scheduleParallelism must be greater than 0");
        Preconditions.checkNotNull(schedulerFactory, "schedulerFactory must be not null");

        this.worker = worker;
        this.scheduler = ThreadPoolUtils.scheduledThreadPoolBuilder()
                .coreThreads(scheduleParallelism)
                .threadFactory(schedulerFactory)
                //默认future cancel时移除task queue, 稍微加大cpu消耗以及阻塞, 以减少堆内存消耗
                .setRemoveOnCancelPolicy()
                .build();
    }

    public ExecutionContext(ExecutorService worker, ScheduledExecutorService scheduler) {
        this.worker = worker;
        this.scheduler = scheduler;
    }

    //--------------------------------------------------------------------------------------------
    public static ExecutionContext forkJoin(int parallelism, String workerNamePrefix) {
        return forkJoin(parallelism, workerNamePrefix, 0);
    }

    public static ExecutionContext forkJoin(int parallelism, String workerNamePrefix, int scheduleParallelism) {
        return forkJoin(parallelism, workerNamePrefix, null, scheduleParallelism);
    }

    public static ExecutionContext forkJoin(int parallelism, String workerNamePrefix, Thread.UncaughtExceptionHandler handler, int scheduleParallelism) {
        ForkJoinPool forkJoinPool = ThreadPoolUtils.forkJoinThreadPoolBuilder()
                .poolName(workerNamePrefix)
                .parallelism(parallelism)
                .threadFactory(new SimpleForkJoinWorkerThreadFactory(workerNamePrefix))
                .uncaughtExceptionHandler(handler)
                .build();
        if (scheduleParallelism > 0) {
            return new ExecutionContext(forkJoinPool, scheduleParallelism, new SimpleThreadFactory(workerNamePrefix.concat(DEFAULT_SCHEDULER_NAME)));
        } else {
            return new ExecutionContext(forkJoinPool);
        }
    }

    public static ExecutionContext asyncForkJoin(int parallelism, String workerNamePrefix) {
        return asyncForkJoin(parallelism, workerNamePrefix, 0);
    }

    public static ExecutionContext asyncForkJoin(int parallelism, String workerNamePrefix, int scheduleParallelism) {
        return asyncForkJoin(parallelism, workerNamePrefix, null, scheduleParallelism);
    }

    public static ExecutionContext asyncForkJoin(int parallelism, String workerNamePrefix, Thread.UncaughtExceptionHandler handler, int scheduleParallelism) {
        ForkJoinPool forkJoinPool = ThreadPoolUtils.forkJoinThreadPoolBuilder()
                .poolName(workerNamePrefix)
                .parallelism(parallelism)
                .threadFactory(new SimpleForkJoinWorkerThreadFactory(workerNamePrefix))
                .uncaughtExceptionHandler(handler)
                .async()
                .build();
        if (scheduleParallelism > 0) {
            return new ExecutionContext(forkJoinPool, scheduleParallelism, new SimpleThreadFactory(workerNamePrefix.concat(DEFAULT_SCHEDULER_NAME)));
        } else {
            return new ExecutionContext(forkJoinPool);
        }
    }

    public static ExecutionContext cache(String workerNamePrefix) {
        return cache(0, Integer.MAX_VALUE, workerNamePrefix, 0, workerNamePrefix.concat(DEFAULT_SCHEDULER_NAME));
    }

    public static ExecutionContext cache(int maxParallelism, String workerNamePrefix) {
        return cache(0, maxParallelism, workerNamePrefix, 0);
    }

    public static ExecutionContext cache(int coreParallelism, int maxParallelism, String workerNamePrefix) {
        return cache(coreParallelism, maxParallelism, workerNamePrefix, 0);
    }

    public static ExecutionContext cache(String workerNamePrefix, int scheduleParallelism) {
        return cache(Integer.MAX_VALUE, workerNamePrefix, scheduleParallelism);
    }

    public static ExecutionContext cache(int maxParallelism, String workerNamePrefix, int scheduleParallelism) {
        return cache(0, maxParallelism, workerNamePrefix, scheduleParallelism);
    }

    public static ExecutionContext cache(int coreParallelism, int maxParallelism, String workerNamePrefix, int scheduleParallelism) {
        return cache(coreParallelism, maxParallelism, workerNamePrefix, scheduleParallelism, workerNamePrefix.concat(DEFAULT_SCHEDULER_NAME));
    }

    public static ExecutionContext cache(int coreParallelism, int maxParallelism, String workerNamePrefix, int scheduleParallelism, String schedulerNamePrefix) {
        return cache(coreParallelism, maxParallelism, new SimpleThreadFactory(workerNamePrefix), scheduleParallelism, new SimpleThreadFactory(schedulerNamePrefix));
    }

    public static ExecutionContext cache(int coreParallelism, int maxParallelism, ThreadFactory workerThreadFactory, int scheduleParallelism, ThreadFactory schedulerFactory) {
        ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtils.threadPoolBuilder()
                .coreThreads(coreParallelism)
                .maximumThreads(maxParallelism)
                .keepAlive(60L, TimeUnit.SECONDS)
                .workQueue(new SynchronousQueue<>())
                .threadFactory(workerThreadFactory)
                .common();
        if (scheduleParallelism > 0) {
            return new ExecutionContext(threadPoolExecutor, scheduleParallelism, schedulerFactory);
        } else {
            return new ExecutionContext(threadPoolExecutor);
        }
    }

    public static ExecutionContext fix(int parallelism, String workerNamePrefix) {
        return fix(parallelism, workerNamePrefix, 0);
    }

    public static ExecutionContext fix(int parallelism, int queue, String workerNamePrefix) {
        return fix(parallelism, queue, workerNamePrefix, 0);
    }

    public static ExecutionContext fix(int parallelism, ThreadFactory workerThreadFactory) {
        return fix(parallelism, workerThreadFactory, 0, null);
    }

    public static ExecutionContext fix(int parallelism, int queue, ThreadFactory workerThreadFactory) {
        return fix(parallelism, queue, workerThreadFactory, 0, null);
    }

    public static ExecutionContext fix(int parallelism, String workerNamePrefix, int scheduleParallelism) {
        return fix(parallelism, workerNamePrefix, scheduleParallelism, workerNamePrefix.concat(DEFAULT_SCHEDULER_NAME));
    }

    public static ExecutionContext fix(int parallelism, int queue, String workerNamePrefix, int scheduleParallelism) {
        return fix(parallelism, queue, workerNamePrefix, scheduleParallelism, workerNamePrefix.concat(DEFAULT_SCHEDULER_NAME));
    }

    public static ExecutionContext fix(int parallelism, String workerNamePrefix, int scheduleParallelism, String schedulerNamePrefix) {
        return fix(parallelism, new SimpleThreadFactory(workerNamePrefix), scheduleParallelism, new SimpleThreadFactory(schedulerNamePrefix));
    }

    public static ExecutionContext fix(int parallelism, int queue, String workerNamePrefix, int scheduleParallelism, String schedulerNamePrefix) {
        return fix(parallelism, queue, new SimpleThreadFactory(workerNamePrefix), scheduleParallelism, new SimpleThreadFactory(schedulerNamePrefix));
    }

    public static ExecutionContext fix(int parallelism, ThreadFactory workerThreadFactory, int scheduleParallelism, ThreadFactory schedulerFactory) {
        return fix(parallelism, Integer.MAX_VALUE, workerThreadFactory, scheduleParallelism, schedulerFactory);
    }

    public static ExecutionContext fix(int parallelism, int queue, ThreadFactory workerThreadFactory, int scheduleParallelism, ThreadFactory schedulerFactory) {
        ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtils.threadPoolBuilder()
                .coreThreads(parallelism)
                .maximumThreads(parallelism)
                .keepAlive(60L, TimeUnit.SECONDS)
                .workQueue(queue)
                .threadFactory(workerThreadFactory)
                .common();
        if (scheduleParallelism > 0) {
            return new ExecutionContext(threadPoolExecutor, scheduleParallelism, schedulerFactory);
        } else {
            return new ExecutionContext(threadPoolExecutor);
        }
    }

    public static ExecutionContext elastic(int coreParallelism, int maxParallelism, String workerNamePrefix) {
        return elastic(coreParallelism, maxParallelism, workerNamePrefix, 0);
    }

    public static ExecutionContext elastic(int coreParallelism, int maxParallelism, int queue, String workerNamePrefix) {
        return elastic(coreParallelism, maxParallelism, queue, workerNamePrefix, 0);
    }

    public static ExecutionContext elastic(int coreParallelism, int maxParallelism, ThreadFactory workerThreadFactory) {
        return elastic(coreParallelism, maxParallelism, workerThreadFactory, 0, null);
    }

    public static ExecutionContext elastic(int coreParallelism, int maxParallelism, int queue, ThreadFactory workerThreadFactory) {
        return elastic(coreParallelism, maxParallelism, queue, workerThreadFactory, 0, null);
    }

    /**
     * 有界扩容的线程池, 允许线程数扩容到一定程度(比如, 10倍CPU核心数), 如果超过这个能力, 则buffer
     */
    public static ExecutionContext elastic(int coreParallelism, int maxParallelism, String workerNamePrefix, int scheduleParallelism) {
        return elastic(coreParallelism, maxParallelism, new SimpleThreadFactory(workerNamePrefix), scheduleParallelism, new SimpleThreadFactory(workerNamePrefix.concat(DEFAULT_SCHEDULER_NAME)));
    }

    /**
     * 有界扩容的线程池, 允许线程数扩容到一定程度(比如, 10倍CPU核心数), 如果超过这个能力, 则buffer
     */
    public static ExecutionContext elastic(int coreParallelism, int maxParallelism, int queue, String workerNamePrefix, int scheduleParallelism) {
        return elastic(coreParallelism, maxParallelism, queue, new SimpleThreadFactory(workerNamePrefix), scheduleParallelism, new SimpleThreadFactory(workerNamePrefix.concat(DEFAULT_SCHEDULER_NAME)));
    }

    /**
     * 有界扩容的线程池, 允许线程数扩容到一定程度(比如, 10倍CPU核心数), 如果超过这个能力, 则buffer
     */
    public static ExecutionContext elastic(int coreParallelism, int maxParallelism, ThreadFactory workerThreadFactory, int scheduleParallelism, ThreadFactory schedulerFactory) {
        return elastic(coreParallelism, maxParallelism, 0, workerThreadFactory, scheduleParallelism, schedulerFactory);
    }

    /**
     * 有界扩容的线程池, 允许线程数扩容到一定程度(比如, 10倍CPU核心数), 如果超过这个能力, 则buffer
     */
    public static ExecutionContext elastic(int coreParallelism, int maxParallelism, int queue, ThreadFactory workerThreadFactory, int scheduleParallelism, ThreadFactory schedulerFactory) {
        EagerThreadPoolExecutor eagerThreadPoolExecutor = ThreadPoolUtils.threadPoolBuilder()
                .coreThreads(coreParallelism)
                .maximumThreads(maxParallelism)
                .keepAlive(60L, TimeUnit.SECONDS)
                .threadFactory(workerThreadFactory)
                .eager(queue);
        if (scheduleParallelism > 0) {
            return new ExecutionContext(eagerThreadPoolExecutor, scheduleParallelism, schedulerFactory);
        } else {
            return new ExecutionContext(eagerThreadPoolExecutor);
        }
    }
    //--------------------------------------------------------------------------------------------

    @Override
    public ScheduledFuture<?> schedule(@Nonnull Runnable command, long delay, @Nonnull TimeUnit unit) {
        Preconditions.checkNotNull(scheduler);
        if (isStopped) {
            throw new IllegalStateException("threads is stopped");
        }

        return scheduler.schedule(() -> execute(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(@Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit unit) {
        Preconditions.checkNotNull(scheduler);
        if (isStopped) {
            throw new IllegalStateException("threads is stopped");
        }

        return scheduler.schedule(() -> submit(callable).get(), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(@Nonnull Runnable command, long initialDelay, long period, @Nonnull TimeUnit unit) {
        Preconditions.checkNotNull(scheduler);
        if (isStopped) {
            throw new IllegalStateException("threads is stopped");
        }

        return scheduler.scheduleAtFixedRate(() -> execute(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(@Nonnull Runnable command, long initialDelay, long delay, @Nonnull TimeUnit unit) {
        Preconditions.checkNotNull(scheduler);
        if (isStopped) {
            throw new IllegalStateException("threads is stopped");
        }

        return scheduler.scheduleWithFixedDelay(() -> execute(command), initialDelay, delay, unit);
    }

    @Override
    public void shutdown() {
        if (isStopped) {
            return;
        }

        isStopped = true;
        worker.shutdown();
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        if (isStopped) {
            return Collections.emptyList();
        }

        isStopped = true;
        List<Runnable> tasks = Lists.newArrayList();
        tasks.addAll(worker.shutdownNow());
        if (scheduler != null) {
            tasks.addAll(scheduler.shutdownNow());
        }
        return tasks;
    }

    @Override
    public boolean isShutdown() {
        return isStopped;
    }

    @Override
    public boolean isTerminated() {
        return isStopped;
    }

    @Override
    public boolean awaitTermination(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        boolean result = worker.awaitTermination(timeout, unit);
        if (scheduler != null) {
            result &= scheduler.awaitTermination(timeout, unit);
        }
        return result;
    }

    @Override
    public <T> Future<T> submit(@Nonnull Callable<T> task) {
        if (isStopped) {
            throw new IllegalStateException("threads is stopped");
        }

        return worker.submit(task);
    }

    @Override
    public <T> Future<T> submit(@Nonnull Runnable task, T result) {
        if (isStopped) {
            throw new IllegalStateException("threads is stopped");
        }

        return worker.submit(task, result);
    }

    @Override
    public Future<?> submit(@Nonnull Runnable task) {
        if (isStopped) {
            throw new IllegalStateException("threads is stopped");
        }

        return worker.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException {
        if (isStopped) {
            throw new IllegalStateException("threads is stopped");
        }

        return worker.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        if (isStopped) {
            throw new IllegalStateException("threads is stopped");
        }

        return worker.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        if (isStopped) {
            throw new IllegalStateException("threads is stopped");
        }

        return worker.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (isStopped) {
            throw new IllegalStateException("threads is stopped");
        }

        return worker.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        if (isStopped) {
            return;
        }
        worker.execute(command);
    }

    /**
     * 是否包含调度线程
     */
    public boolean withScheduler() {
        return Objects.nonNull(scheduler) && !scheduler.isShutdown();
    }
}
