package org.kin.framework.event;

import org.kin.framework.utils.OrderedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public void handle(EventBus bus, T event) throws Exception {
        for (EventHandler<T> handler : handlers) {
            try {
                handler.handle(bus, event);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    /**
     * 基于copy on write更新
     */
    synchronized void addHandler(EventHandler<T> handler) {
        List<EventHandler<T>> handlers = new ArrayList<>(this.handlers.size() + 1);
        handlers.add(handler);
        OrderedUtils.sort(handlers);
        this.handlers = handlers;
    }

    //getter
    List<EventHandler<T>> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }
}
