package org.kin.framework.event;

import org.kin.framework.common.Ordered;

import java.util.Objects;
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
    void handle(EventBus eventBus, T event);

    /**
     * @return  事件处理的 {@link Executor}实现类
     */
    default Executor executor(){
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void handleEvent(EventHandler eventHandler, EventBus eventBus, Object event){
        Executor executor = eventHandler.executor();
        if (Objects.nonNull(executor)) {
            executor.execute(() -> eventHandler.handle(eventBus, event));
        }
        else{
            eventHandler.handle(eventBus, event);
        }
    }
}
