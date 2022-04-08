package org.kin.framework.event;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 事件总线接口
 *
 * @author 健勤
 * @date 2017/8/8
 */
public interface EventBus {
    /**
     * 注册{@link EventHandler}
     *
     * @param obj {@link EventHandler}实现类或者public方法带有{@link EventFunction} 注解的实例
     */
    void register(Object obj);

    /**
     * 直接执行task
     *
     * @param task 任务逻辑
     */
    void post(Runnable task);

    /**
     * 分发事件
     *
     * @param event 事件实例
     */
    void post(Object event);

    /**
     * 延迟调度事件分发
     *
     * @param event 事件
     * @param delay  延迟执行时间
     * @param unit  时间单位
     */
    Future<?> schedule(Object event, long delay, TimeUnit unit);

    /**
     * 固定时间间隔调度事件分发
     *
     * @param event        事件
     * @param initialDelay 延迟执行时间
     * @param period       时间间隔
     * @param unit         时间单位
     */
    Future<?> scheduleAtFixRate(Object event, long initialDelay, long period, TimeUnit unit);

    /**
     * 固定时间延迟调度事件分发
     *
     * @param event        事件
     * @param initialDelay 延迟执行时间
     * @param delay        时间延迟
     * @param unit         时间单位
     */
    Future<?> scheduleWithFixedDelay(Object event, long initialDelay, long delay, TimeUnit unit);

    /**
     * shutdown
     */
    void shutdown();
}
