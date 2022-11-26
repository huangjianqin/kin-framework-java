package org.kin.framework.concurrent;

import org.kin.framework.JvmCloseCleaner;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 当前JVM内存统计
 *
 * @author huangjianqin
 * @date 2022/9/6
 */
public final class MemoryCalculator {
    /** 当前JVM最大可用内存, 每50ms刷新一次 */
    private static volatile long maxAvailable;

    /** 刷线线程是否已开启 */
    private static final AtomicBoolean refreshStarted = new AtomicBoolean(false);

    /**
     * 刷新JVM最大可用内存
     */
    private static void refresh() {
        maxAvailable = Runtime.getRuntime().freeMemory();
    }

    /**
     * 检查刷线线程是否已开启, 如果没有, 则开启
     */
    private static void checkAndScheduleRefresh() {
        if (!refreshStarted.get()) {
            //每次调用方法时都会检查, 故先刷新一次, 防止第一次使用时, maxAvailable=0(刷新线程还没来得及刷新)
            //注意, 这里可能会被多个线程触发调用(最终只有一个线程启动刷新线程), 因为没有加锁
            refresh();
            if (refreshStarted.compareAndSet(false, true)) {
                String name = "memory-calculator";
                ScheduledExecutorService scheduler = ThreadPoolUtils.newScheduledThreadPool(name, true, 1, new SimpleThreadFactory(name, true));
                //50ms刷新一次
                scheduler.scheduleWithFixedDelay(MemoryCalculator::refresh, 50, 50, TimeUnit.MILLISECONDS);
                JvmCloseCleaner.instance().add(scheduler::shutdownNow);
            }
        }
    }

    /**
     * 返回JVM最大可用内存
     *
     * @return JVM最大可用内存
     */
    public static long maxAvailable() {
        checkAndScheduleRefresh();
        return maxAvailable;
    }

    /**
     * 以当前JVM最大可用内存为基础, 按{@code percentage}百分比, 计算可用内存
     *
     * @param percentage 百分比
     * @return 可用内存
     */
    public static long calculate(final double percentage) {
        if (percentage <= 0 || percentage > 1) {
            throw new IllegalArgumentException();
        }
        checkAndScheduleRefresh();
        return (long) (maxAvailable() * percentage);
    }

    /**
     * 默认, 返回80%的当前JVM最大可用内存, 以作为内存限制大小
     *
     * @return 可用内存
     */
    public static long defaultLimit() {
        checkAndScheduleRefresh();
        return calculate(0.8);
    }
}
