package org.kin.framework.concurrent;

import org.kin.framework.utils.StringUtils;
import org.kin.framework.utils.SysUtils;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.*;

/**
 * @author huangjianqin
 * @date 2021/10/14
 */
public final class ThreadPoolUtils {
    /**
     * The default rejected execution handler
     */
    private static final java.util.concurrent.RejectedExecutionHandler DEFAULT_REJECTED_EXECUTION_HANDLER = new ThreadPoolExecutor.AbortPolicy();

    private ThreadPoolUtils() {
    }

    public static ThreadPoolBuilder threadPoolBuilder() {
        return new ThreadPoolBuilder();
    }

    public static ForkJoinThreadPoolBuilder forkJoinThreadPoolBuilder() {
        return new ForkJoinThreadPoolBuilder();
    }

    public static ScheduledThreadPoolBuilder scheduledThreadPoolBuilder() {
        return new ScheduledThreadPoolBuilder();
    }

    public static ThreadPoolExecutor newThreadPool(String poolName, boolean enableMetric,
                                                   int coreThreads, int maximumThreads,
                                                   long keepAliveTime, TimeUnit unit,
                                                   BlockingQueue<Runnable> workQueue,
                                                   ThreadFactory threadFactory) {
        return newThreadPool(poolName, enableMetric, coreThreads, maximumThreads, keepAliveTime, unit, workQueue,
                threadFactory, DEFAULT_REJECTED_EXECUTION_HANDLER);
    }

    public static ThreadPoolExecutor newThreadPool(String poolName, boolean enableMetric,
                                                   int coreThreads, int maximumThreads,
                                                   long keepAliveTime, TimeUnit unit,
                                                   BlockingQueue<Runnable> workQueue,
                                                   ThreadFactory threadFactory,
                                                   java.util.concurrent.RejectedExecutionHandler rejectedHandler) {
        if (enableMetric) {
            return new MetricThreadPoolExecutor(coreThreads, maximumThreads, keepAliveTime, unit, workQueue,
                    threadFactory, rejectedHandler, poolName);
        } else {
            return new MonitorableThreadPoolExecutor(coreThreads, maximumThreads, keepAliveTime, unit, workQueue,
                    threadFactory, rejectedHandler, poolName);
        }
    }

    public static EagerThreadPoolExecutor newEagerThreadPool(String poolName, boolean enableMetric,
                                                             int coreThreads, int maximumThreads,
                                                             long keepAliveTime, TimeUnit unit,
                                                             int queueSize,
                                                             ThreadFactory threadFactory) {
        return newEagerThreadPool(poolName, enableMetric, coreThreads, maximumThreads,
                keepAliveTime, unit, queueSize, threadFactory, DEFAULT_REJECTED_EXECUTION_HANDLER);
    }

    public static EagerThreadPoolExecutor newEagerThreadPool(String poolName, boolean enableMetric,
                                                             int coreThreads, int maximumThreads,
                                                             long keepAliveTime, TimeUnit unit,
                                                             int queueSize,
                                                             ThreadFactory threadFactory,
                                                             java.util.concurrent.RejectedExecutionHandler rejectedHandler) {
        if (enableMetric) {
            return MetricEagerThreadPoolExecutor.create(poolName, coreThreads, maximumThreads, keepAliveTime, unit, queueSize,
                    threadFactory, rejectedHandler);
        } else {
            return MonitorableEagerThreadPoolExecutor.create(poolName, coreThreads, maximumThreads, keepAliveTime, unit, queueSize,
                    threadFactory, rejectedHandler);
        }
    }

    public static ForkJoinPool newForkJoinPool(String poolName, boolean enableMetric,
                                               int parallelism, Thread.UncaughtExceptionHandler handler, boolean asyncMode) {
        return newForkJoinPool(poolName, enableMetric, parallelism, ForkJoinPool.defaultForkJoinWorkerThreadFactory, handler, asyncMode);
    }

    public static ForkJoinPool newForkJoinPool(String poolName, boolean enableMetric,
                                               int parallelism, ForkJoinPool.ForkJoinWorkerThreadFactory factory,
                                               Thread.UncaughtExceptionHandler handler, boolean asyncMode) {
        if (enableMetric) {
            return new MetricForkJoinPool(parallelism, factory, handler, asyncMode, poolName);
        } else {
            return new MonitorableForkJoinPool(parallelism, factory, handler, asyncMode, poolName);
        }
    }

    public static ScheduledThreadPoolExecutor newScheduledThreadPool(String poolName, boolean enableMetric,
                                                                     int coreThreads,
                                                                     ThreadFactory threadFactory) {
        return newScheduledThreadPool(poolName, enableMetric, coreThreads, threadFactory, DEFAULT_REJECTED_EXECUTION_HANDLER);
    }

    public static ScheduledThreadPoolExecutor newScheduledThreadPool(String poolName, boolean enableMetric,
                                                                     int coreThreads,
                                                                     ThreadFactory threadFactory,
                                                                     java.util.concurrent.RejectedExecutionHandler rejectedHandler) {
        if (enableMetric) {
            return new MetricScheduledThreadPoolExecutor(coreThreads, threadFactory, rejectedHandler, poolName);
        } else {
            return new MonitorableScheduledThreadPoolExecutor(coreThreads, threadFactory, rejectedHandler, poolName);
        }
    }

    /**
     * 如果没有设置pool name, 则尝试从{@link ThreadFactory}获取, 否则undefined
     */
    static String applyPoolNameIfBlank(String name, ThreadFactory threadFactory) {
        if (StringUtils.isNotBlank(name)) {
            return name;
        }

        if (Objects.nonNull(threadFactory)) {
            //尝试从SimpleThreadFactory获取pool name
            if (threadFactory instanceof SimpleThreadFactory) {
                SimpleThreadFactory simpleThreadFactory = (SimpleThreadFactory) threadFactory;
                return simpleThreadFactory.getPrefix();
            }
        }

        return "undefined";
    }

    //------------------------------------------------------------------------------------
    public static final class ThreadPoolBuilder {
        private String poolName;
        private boolean enableMetric;
        private int coreThreads = SysUtils.getSuitableThreadNum();
        private int maximumThreads = Integer.MAX_VALUE;
        private long keepAliveTime = 60L;
        private TimeUnit unit = TimeUnit.SECONDS;
        private BlockingQueue<Runnable> workQueue;
        private ThreadFactory threadFactory;
        private java.util.concurrent.RejectedExecutionHandler handler = ThreadPoolUtils.DEFAULT_REJECTED_EXECUTION_HANDLER;
        private boolean allowCoreThreadTimeOut;

        private ThreadPoolBuilder() {
        }

        public ThreadPoolBuilder poolName(String poolName) {
            this.poolName = poolName;
            return this;
        }

        public ThreadPoolBuilder metric() {
            this.enableMetric = true;
            return this;
        }

        public ThreadPoolBuilder coreThreads(int coreThreads) {
            this.coreThreads = coreThreads;
            return this;
        }

        public ThreadPoolBuilder maximumThreads(int maximumThreads) {
            this.maximumThreads = maximumThreads;
            return this;
        }

        public ThreadPoolBuilder keepAliveSeconds(long keepAliveSeconds) {
            return keepAlive(keepAliveSeconds, TimeUnit.SECONDS);
        }

        public ThreadPoolBuilder keepAlive(long keepAliveTime, TimeUnit unit) {
            this.keepAliveTime = keepAliveTime;
            this.unit = unit;
            return this;
        }

        public ThreadPoolBuilder workQueue(int queueLen) {
            return workQueue(new LinkedBlockingQueue<>(queueLen));
        }

        public ThreadPoolBuilder workQueue(BlockingQueue<Runnable> workQueue) {
            this.workQueue = workQueue;
            return this;
        }

        public ThreadPoolBuilder threadFactory(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            return this;
        }

        public ThreadPoolBuilder rejectedHandler(java.util.concurrent.RejectedExecutionHandler handler) {
            this.handler = handler;
            return this;
        }

        public ThreadPoolBuilder allowCoreThreadTimeOut() {
            this.allowCoreThreadTimeOut = true;
            return this;
        }

        //---------------------------------------------build
        public ThreadPoolExecutor common() {
            beforeConstruct(true);
            ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtils.newThreadPool(this.poolName, this.enableMetric, this.coreThreads,
                    this.maximumThreads, this.keepAliveTime, this.unit, this.workQueue, this.threadFactory, this.handler);
            afterConstruct(threadPoolExecutor);
            return threadPoolExecutor;
        }

        public EagerThreadPoolExecutor eager() {
            beforeConstruct(false);
            EagerThreadPoolExecutor threadPoolExecutor = eager(0);
            afterConstruct(threadPoolExecutor);
            return threadPoolExecutor;
        }

        public EagerThreadPoolExecutor eager(int queueSize) {
            beforeConstruct(false);
            EagerThreadPoolExecutor threadPoolExecutor = ThreadPoolUtils.newEagerThreadPool(this.poolName, this.enableMetric, this.coreThreads,
                    this.maximumThreads, this.keepAliveTime, this.unit, queueSize, this.threadFactory, this.handler);
            afterConstruct(threadPoolExecutor);
            return threadPoolExecutor;
        }

        /**
         * @param defaultWorkQueue  如果没有设置workQueue, 则初始化默认
         */
        private void beforeConstruct(boolean defaultWorkQueue) {
            if (defaultWorkQueue && Objects.isNull(workQueue)) {
                workQueue = new MemorySafeLinkedBlockingQueue<>();
            }
            if (Objects.isNull(threadFactory)) {
                threadFactory = Executors.defaultThreadFactory();
            }
        }

        private void afterConstruct(ThreadPoolExecutor threadPoolExecutor) {
            if (allowCoreThreadTimeOut) {
                threadPoolExecutor.allowCoreThreadTimeOut(true);
            }
        }
    }

    public static final class ScheduledThreadPoolBuilder {
        private String poolName;
        private boolean enableMetric;
        private int coreThreads = SysUtils.getSuitableThreadNum();
        private ThreadFactory threadFactory;
        private java.util.concurrent.RejectedExecutionHandler handler = ThreadPoolUtils.DEFAULT_REJECTED_EXECUTION_HANDLER;
        private boolean allowCoreThreadTimeOut;
        private boolean setRemoveOnCancelPolicy;

        private ScheduledThreadPoolBuilder() {
        }

        public ScheduledThreadPoolBuilder poolName(String poolName) {
            this.poolName = poolName;
            return this;
        }

        public ScheduledThreadPoolBuilder metric() {
            this.enableMetric = true;
            return this;
        }

        public ScheduledThreadPoolBuilder coreThreads(int coreThreads) {
            this.coreThreads = coreThreads;
            return this;
        }

        public ScheduledThreadPoolBuilder threadFactory(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            return this;
        }

        public ScheduledThreadPoolBuilder rejectedHandler(RejectedExecutionHandler handler) {
            this.handler = handler;
            return this;
        }

        public ScheduledThreadPoolBuilder allowCoreThreadTimeOut() {
            this.allowCoreThreadTimeOut = true;
            return this;
        }

        public ScheduledThreadPoolBuilder setRemoveOnCancelPolicy() {
            this.setRemoveOnCancelPolicy = true;
            return this;
        }

        public ScheduledThreadPoolExecutor build() {
            if (Objects.isNull(threadFactory)) {
                threadFactory = Executors.defaultThreadFactory();
            }

            ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadPoolUtils.newScheduledThreadPool(this.poolName, this.enableMetric, this.coreThreads,
                    this.threadFactory, this.handler);
            if (allowCoreThreadTimeOut) {
                scheduledThreadPoolExecutor.allowCoreThreadTimeOut(true);
            }
            if (setRemoveOnCancelPolicy) {
                scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
            }
            return scheduledThreadPoolExecutor;
        }
    }

    public static final class ForkJoinThreadPoolBuilder {
        private String poolName;
        private boolean enableMetric;
        private int parallelism = SysUtils.CPU_NUM;
        private ForkJoinPool.ForkJoinWorkerThreadFactory factory;
        private Thread.UncaughtExceptionHandler handler;
        private boolean asyncMode;

        public ForkJoinThreadPoolBuilder poolName(String poolName) {
            this.poolName = poolName;
            return this;
        }

        public ForkJoinThreadPoolBuilder metric() {
            this.enableMetric = true;
            return this;
        }

        public ForkJoinThreadPoolBuilder parallelism(int parallelism) {
            this.parallelism = parallelism;
            return this;
        }

        public ForkJoinThreadPoolBuilder threadFactory(ForkJoinPool.ForkJoinWorkerThreadFactory factory) {
            this.factory = factory;
            return this;
        }

        public ForkJoinThreadPoolBuilder uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
            this.handler = handler;
            return this;
        }

        public ForkJoinThreadPoolBuilder async() {
            this.asyncMode = true;
            return this;
        }

        public ForkJoinPool build() {
            if (Objects.isNull(factory)) {
                factory = ForkJoinPool.defaultForkJoinWorkerThreadFactory;
            }
            return ThreadPoolUtils.newForkJoinPool(this.poolName, this.enableMetric, this.parallelism,
                    this.factory, this.handler, this.asyncMode);
        }
    }
}
