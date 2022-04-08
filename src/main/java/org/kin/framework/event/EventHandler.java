package org.kin.framework.event;

import org.kin.framework.common.Ordered;

import java.util.concurrent.Executor;

/**
 * 事件处理器
 *
 * @author 健勤
 * @date 2017/8/8
 */
public interface EventHandler<T> extends Ordered {
    /**
     * 事件处理逻辑
     *
     * @param event 事件类
     */
    void handle(EventBus bus, T event) throws Exception;

    /**
     * @return  事件处理的 {@link Executor}实现类
     */
    default Executor executor(){
        return null;
    }
}
