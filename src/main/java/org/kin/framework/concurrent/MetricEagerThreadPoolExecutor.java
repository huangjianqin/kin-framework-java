package org.kin.framework.concurrent;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author huangjianqin
 * @date 2021/10/15
 */
public class MetricEagerThreadPoolExecutor extends MonitorableEagerThreadPoolExecutor {
    private static final ThreadLocal<Timer.Sample> TIMER_SAMPLE_THREAD_LOCAL = new ThreadLocal<>();

    public static MetricEagerThreadPoolExecutor create(String name,
                                                       int corePoolSize,
                                                       int maximumPoolSize,
                                                       long keepAliveTime,
                                                       TimeUnit unit) {
        return create(name, corePoolSize, maximumPoolSize, keepAliveTime, unit, 0);
    }

    public static MetricEagerThreadPoolExecutor create(String name,
                                                       int corePoolSize,
                                                       int maximumPoolSize,
                                                       long keepAliveTime,
                                                       TimeUnit unit,
                                                       RejectedExecutionHandler handler) {
        return create(name, corePoolSize, maximumPoolSize, keepAliveTime, unit, 0, handler);
    }

    public static MetricEagerThreadPoolExecutor create(String name,
                                                       int corePoolSize,
                                                       int maximumPoolSize,
                                                       long keepAliveTime,
                                                       TimeUnit unit,
                                                       ThreadFactory threadFactory) {
        return create(name, corePoolSize, maximumPoolSize, keepAliveTime, unit, 0, threadFactory);
    }

    public static MetricEagerThreadPoolExecutor create(String name,
                                                       int corePoolSize,
                                                       int maximumPoolSize,
                                                       long keepAliveTime,
                                                       TimeUnit unit,
                                                       ThreadFactory threadFactory,
                                                       RejectedExecutionHandler handler) {
        return create(name, corePoolSize, maximumPoolSize, keepAliveTime, unit, 0, threadFactory, handler);
    }

    private static EagerTaskQueue<Runnable> create(int queueSize) {
        return queueSize > 0 ? new EagerTaskQueue<>(queueSize) : new EagerTaskQueue<>();
    }

    public static MetricEagerThreadPoolExecutor create(String name,
                                                       int corePoolSize,
                                                       int maximumPoolSize,
                                                       long keepAliveTime,
                                                       TimeUnit unit,
                                                       int queueSize) {
        EagerTaskQueue<Runnable> workQueue = create(queueSize);
        MetricEagerThreadPoolExecutor executor = new MetricEagerThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, name);
        workQueue.updateExecutor(executor);
        return executor;
    }

    public static MetricEagerThreadPoolExecutor create(String name,
                                                       int corePoolSize,
                                                       int maximumPoolSize,
                                                       long keepAliveTime,
                                                       TimeUnit unit,
                                                       int queueSize,
                                                       RejectedExecutionHandler handler) {
        EagerTaskQueue<Runnable> workQueue = create(queueSize);
        MetricEagerThreadPoolExecutor executor = new MetricEagerThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler, name);
        workQueue.updateExecutor(executor);
        return executor;
    }

    public static MetricEagerThreadPoolExecutor create(String name,
                                                       int corePoolSize,
                                                       int maximumPoolSize,
                                                       long keepAliveTime,
                                                       TimeUnit unit,
                                                       int queueSize,
                                                       ThreadFactory threadFactory) {
        EagerTaskQueue<Runnable> workQueue = create(queueSize);
        MetricEagerThreadPoolExecutor executor = new MetricEagerThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, name);
        workQueue.updateExecutor(executor);
        return executor;
    }

    public static MetricEagerThreadPoolExecutor create(String name,
                                                       int corePoolSize,
                                                       int maximumPoolSize,
                                                       long keepAliveTime,
                                                       TimeUnit unit,
                                                       int queueSize,
                                                       ThreadFactory threadFactory,
                                                       RejectedExecutionHandler handler) {
        EagerTaskQueue<Runnable> workQueue = create(queueSize);
        MetricEagerThreadPoolExecutor executor = new MetricEagerThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler, name);
        workQueue.updateExecutor(executor);
        return executor;
    }

    protected MetricEagerThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                            long keepAliveTime, TimeUnit unit,
                                            EagerTaskQueue<Runnable> workQueue, String name) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, name);
    }

    protected MetricEagerThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                            long keepAliveTime, TimeUnit unit,
                                            EagerTaskQueue<Runnable> workQueue, RejectedExecutionHandler handler,
                                            String name) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler, name);
    }

    protected MetricEagerThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                            long keepAliveTime, TimeUnit unit,
                                            EagerTaskQueue<Runnable> workQueue, ThreadFactory threadFactory,
                                            String name) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, name);
    }

    protected MetricEagerThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                            long keepAliveTime, TimeUnit unit,
                                            EagerTaskQueue<Runnable> workQueue, ThreadFactory threadFactory,
                                            RejectedExecutionHandler handler, String name) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler, name);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        TIMER_SAMPLE_THREAD_LOCAL.set(Timer.start(Metrics.globalRegistry));
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        Timer.Sample sample = TIMER_SAMPLE_THREAD_LOCAL.get();
        if (Objects.nonNull(sample)) {
            sample.stop(Metrics.timer("eagerThreadPool." + getName()));
            TIMER_SAMPLE_THREAD_LOCAL.remove();
        }
    }
}
