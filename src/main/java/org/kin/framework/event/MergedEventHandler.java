package org.kin.framework.event;

import org.kin.framework.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * 支持事件合并的{@link EventHandler}实现
 *
 * @author huangjianqin
 * @date 2022/4/9
 */
class MergedEventHandler<T> implements EventHandler<T> {
    private static final Logger log = LoggerFactory.getLogger(MergedEventHandler.class);

    /** 委托的event handler */
    private final EventHandler<Collection<T>> delegate;
    /** 缓存窗口时间的事件 */
    private List<T> pendingEvents = new ArrayList<>();
    /** 事件合并参数 */
    private final EventMerge eventMerge;
    /** 绑定的{@link DefaultEventBus} */
    private final DefaultEventBus eventBus;
    /** 事件延迟处理future */
    private Future<?> future;

    MergedEventHandler(EventHandler<Collection<T>> delegate, EventMerge eventMerge, DefaultEventBus eventBus) {
        this.delegate = delegate;
        this.eventMerge = eventMerge;
        this.eventBus = eventBus;
    }

    @Override
    public void handle(EventBus eventBus, T event) {
        if (this.eventBus != eventBus) {
            throw new IllegalStateException("event bus is illegal");
        }

        mergeEvent(event);
    }

    /**
     * 事件合并
     */
    private synchronized void mergeEvent(T event) {
        MergeType type = eventMerge.type();
        if (MergeType.WINDOW.equals(type)) {
            mergeWindowEvent(event);
        } else if (MergeType.DEBOUNCE.equals(type)) {
            mergeDebounceEvent(event);
        } else {
            throw new UnsupportedOperationException(String.format("doesn't support merge type '%s'", type));
        }
    }

    /**
     * 分发合并后事件集合
     */
    private void triggerMergedEvents() {
        future = null;
        //合并后事件集合
        List<T> pendingEvents;
        synchronized (this) {
            //合并后事件集合
            pendingEvents = this.pendingEvents;
            //reset
            this.pendingEvents = new ArrayList<>();
        }

        if (CollectionUtils.isEmpty(pendingEvents)) {
            return;
        }

        EventHandler.handleEvent(delegate, eventBus, pendingEvents);
    }

    /**
     * 根据窗口规则(一直调度), 合并事件
     */
    private void mergeWindowEvent(T event) {
        //启动window
        if (Objects.isNull(future)) {
            future = eventBus.getScheduler().scheduleAtFixedRate(this::triggerMergedEvents, eventMerge.window(), eventMerge.window(), eventMerge.unit());
        }

        pendingEvents.add(event);
    }

    /**
     * 根据抖动规则(有event进入队列才触发调度), 合并事件
     */
    private void mergeDebounceEvent(T event) {
        //启动debounce
        if (Objects.isNull(future)) {
            //重置
            future = eventBus.getScheduler().schedule(this::triggerMergedEvents, eventMerge.window(), eventMerge.unit());
        }

        pendingEvents.add(event);
        if (pendingEvents.size() >= eventMerge.maxSize()) {
            //最大窗口大小
            future.cancel(true);
            triggerMergedEvents();
        }
    }

    @Override
    public synchronized void close() {
        //尝试cancel future
        if (Objects.nonNull(future)) {
            //重置
            future.cancel(true);
        }
        //help gc
        future = null;
        try {
            //trigger merged event now
            triggerMergedEvents();

            EventHandler.closeHandler(delegate);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
