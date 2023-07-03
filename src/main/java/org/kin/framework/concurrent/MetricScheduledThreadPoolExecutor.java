package org.kin.framework.concurrent;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;

/**
 * @author huangjianqin
 * @date 2021/10/14
 */
public class MetricScheduledThreadPoolExecutor extends MonitorableScheduledThreadPoolExecutor {
    private static final ThreadLocal<Timer.Sample> TIMER_SAMPLE_THREAD_LOCAL = new ThreadLocal<>();

    public MetricScheduledThreadPoolExecutor(int corePoolSize, String name) {
        super(corePoolSize, name);
    }

    public MetricScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, String name) {
        super(corePoolSize, threadFactory, name);
    }

    public MetricScheduledThreadPoolExecutor(int corePoolSize, java.util.concurrent.RejectedExecutionHandler handler, String name) {
        super(corePoolSize, handler, name);
    }

    public MetricScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory,
                                             RejectedExecutionHandler handler, String name) {
        super(corePoolSize, threadFactory, handler, name);
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
            sample.stop(Metrics.timer("scheduledThreadPool." + getName()));
            TIMER_SAMPLE_THREAD_LOCAL.remove();
        }
    }
}