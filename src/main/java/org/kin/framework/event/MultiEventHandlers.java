package org.kin.framework.event;

import org.kin.framework.utils.OrderedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于支持一个event, 对应多个event handler的场景
 *
 * @author huangjianqin
 * @date 2021/3/12
 */
class MultiEventHandlers<T> implements EventHandler<T> {
    private static final Logger log = LoggerFactory.getLogger(MultiEventHandlers.class);

    /** {@link EventHandler}实现类集合 */
    private volatile List<EventHandler<T>> handlers = new ArrayList<>();

    @Override
    public void handle(EventBus eventBus, T event) {
        for (EventHandler<T> eventHandler : handlers) {
            try {
                EventHandler.handleEvent(eventHandler, eventBus, event);
            } catch (Exception e) {
                log.error("event handler handle event '{}' error {}", event, e);
            }
        }
    }

    @Override
    public void close() {
        for (EventHandler<T> eventHandler : handlers) {
            try {
                EventHandler.closeHandler(eventHandler);
            } catch (Exception e) {
                log.error("event consumer close error, ", e);
            }
        }
    }

    /**
     * 基于copy on write更新
     */
    synchronized void addHandler(EventHandler<T> handler) {
        List<EventHandler<T>> handlers = new ArrayList<>(this.handlers.size() + 1);
        handlers.addAll(this.handlers);
        handlers.add(handler);
        OrderedUtils.sort(handlers);
        this.handlers = handlers;
    }
}
