package org.kin.framework.event;

import org.kin.framework.Closeable;
import org.kin.framework.common.Ordered;

import java.util.EventListener;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * 事件处理器
 *
 * @author 健勤
 * @date 2017/8/8
 */
public interface EventHandler<T> extends EventListener, Closeable, Ordered {
    /**
     * 事件处理逻辑
     *
     * @param event 事件类
     */
    void handle(EventBus eventBus, T event);

    /**
     * @return 事件处理的 {@link Executor}实现类
     */
    default Executor executor(){
        return null;
    }

    @Override
    default void close(){
        //default do nothing
    }

    /**
     * {@link EventHandler}处理event统一处理逻辑
     */
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

    /**
     * {@link EventHandler#close()} 统一处理逻辑
     */
    @SuppressWarnings({"rawtypes"})
    static void closeHandler(EventHandler eventHandler){
        Executor executor = eventHandler.executor();
        if (Objects.nonNull(executor)) {
            executor.execute(eventHandler::close);
        }
        else{
            eventHandler.close();
        }
    }
}
