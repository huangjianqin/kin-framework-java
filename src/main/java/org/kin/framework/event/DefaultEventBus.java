package org.kin.framework.event;

import com.google.common.base.Preconditions;
import org.jctools.maps.NonBlockingHashMap;
import org.kin.framework.concurrent.ExecutionContext;
import org.kin.framework.proxy.MethodDefinition;
import org.kin.framework.proxy.ProxyInvoker;
import org.kin.framework.proxy.Proxys;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.SysUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 事件总线
 * 不保证事件注册的实时性
 * <p>
 * 事件类, 目前事件类最好比较native, 也就是不带泛型的, 也最好不是集合类, 数组等等
 *
 * @author 健勤
 * @date 2017/8/8
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class DefaultEventBus implements EventBus {
    private static final Logger log = LoggerFactory.getLogger(DefaultEventBus.class);

    /** key -> event class, value -> event handler */
    private final Map<Class<?>, EventHandler<?>> event2Handler = new NonBlockingHashMap<>();
    /** 调度线程 */
    private final ExecutionContext scheduler;

    private volatile boolean stopped;

    public DefaultEventBus() {
        this(ExecutionContext.fix(SysUtils.CPU_NUM, "defaultEventBus", 2));
    }

    public DefaultEventBus(ExecutionContext scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void register(Object obj) {
        Preconditions.checkNotNull(obj, "param must be not null");

        if (isStopped()) {
            throw new IllegalStateException("event bus is stopped");
        }

        if (obj instanceof EventHandler) {
            registerEventHandler((EventHandler) obj);
        } else {
            parseEventFuncAndRegister(obj);
        }
    }

    /**
     * 从{@link EventHandler}实现解析出event class并注册
     */
    private void registerEventHandler(EventHandler eventHandler) {
        Class<? extends EventHandler> eventHandlerClass = eventHandler.getClass();
        Class<?> eventType = (Class<?>) ClassUtils.getSuperInterfacesGenericActualTypes(EventHandler.class, eventHandlerClass).get(0);

        registerEventHandler(eventType, eventHandler, eventHandlerClass.getAnnotation(EventMerge.class));
    }

    /**
     * 注册{@link EventHandler}, 该handler可能支持事件合并
     */
    private void registerEventHandler(Class<?> eventType, EventHandler eventHandler, EventMerge eventMerge) {
        if (Objects.isNull(eventMerge)) {
            //不支持事件合并
            registerEventHandler(eventType, eventHandler);
        } else {
            //支持事件合并
            registerEventHandler(eventType, new MergedEventHandler(eventHandler, eventMerge, this));
        }
    }

    /**
     * 注册event class及其对应的{@link EventHandler}实现
     */
    private void registerEventHandler(Class<?> eventType, EventHandler eventHandler) {
        EventHandler<?> registered = event2Handler.get(eventType);
        if (registered == null) {
            event2Handler.put(eventType, eventHandler);
        } else if (!(registered instanceof MultiEventHandlers)) {
            MultiEventHandlers multiHandler = new MultiEventHandlers();
            multiHandler.addHandler(registered);
            multiHandler.addHandler(eventHandler);
            event2Handler.put(eventType, multiHandler);
        } else {
            ((MultiEventHandlers) registered).addHandler(eventHandler);
        }
    }

    /**
     * 解析{@link EventFunction}注解方法并进行注册
     */
    private void parseEventFuncAndRegister(Object obj) {
        Class<?> claxx = obj.getClass();

        if (!claxx.isAnnotationPresent(EventListener.class)) {
            throw new IllegalArgumentException(String.format("%s must be annotated with @%s", obj.getClass(), EventListener.class.getSimpleName()));
        }

        //注解在方法
        //在所有  public & 有注解的  方法中寻找一个匹配的方法作为事件处理方法
        for (Method method : claxx.getMethods()) {
            if (!method.isAnnotationPresent(EventFunction.class)) {
                continue;
            }

            Type[] parameterTypes = method.getGenericParameterTypes();
            int paramNum = parameterTypes.length;
            if (paramNum == 0 || paramNum > 2) {
                //只处理一个或两个参数的public方法
                continue;
            }

            //事件类
            Class<?> eventType = null;
            //EventBus实现类的方法参数位置, 默认没有
            int eventBusParamIdx = 0;

            EventMerge eventMerge = method.getAnnotation(EventMerge.class);
            if (Objects.isNull(eventMerge)) {
                //不支持事件合并, (EventBus,Event)
                for (int i = 1; i <= parameterTypes.length; i++) {
                    //普通类型
                    Class<?> parameterType = (Class<?>) parameterTypes[i - 1];
                    if (EventBus.class.isAssignableFrom(parameterType)) {
                        eventBusParamIdx = i;
                    } else {
                        eventType = parameterType;
                    }
                }
            } else {
                //支持事件合并, (EventBus,Collection<Event>)
                for (int i = 1; i <= parameterTypes.length; i++) {
                    if (parameterTypes[i - 1] instanceof ParameterizedType) {
                        //泛型类型
                        ParameterizedType parameterizedType = (ParameterizedType) parameterTypes[i - 1];
                        Class<?> parameterizedRawType = (Class<?>) parameterizedType.getRawType();
                        if (Collection.class.isAssignableFrom(parameterizedRawType)) {
                            //事件合并, 获取集合的泛型
                            //以实际事件类型来注册事件处理器
                            eventType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        } else {
                            throw new IllegalArgumentException("use merged event, param must be collection, but actually " + parameterizedRawType);
                        }
                    } else {
                        //普通类型
                        Class<?> parameterType = (Class<?>) parameterTypes[i - 1];
                        if (EventBus.class.isAssignableFrom(parameterType)) {
                            eventBusParamIdx = i;
                        }
                    }
                }
            }

            EventFunction eventFunctionAnno = method.getAnnotation(EventFunction.class);

            registerEventFunc(eventType, generateEventFuncMethodInvoker(obj, method),
                    eventBusParamIdx, eventFunctionAnno.order(), eventMerge);
        }
    }

    /**
     * 注册基于{@link EventFunction}注入的事件处理方法
     *
     * @param eventBusParamIdx {@link EventBus}实现类的方法参数位置, 默认0, 标识没有该参数
     */
    private void registerEventFunc(Class<?> eventType, ProxyInvoker<?> invoker, int eventBusParamIdx, int order, EventMerge eventMerge) {
        EventFunctionHandler<?> eventHandler = new EventFunctionHandler<>(
                invoker,
                eventBusParamIdx,
                order);
        registerEventHandler(eventType, eventHandler, eventMerge);
    }

    /**
     * @return {@link EventFunction} 注解方法代理类
     */
    private ProxyInvoker<?> generateEventFuncMethodInvoker(Object obj, Method method) {
        return Proxys.adaptive().enhanceMethod(new MethodDefinition<>(obj, method));
    }

    /**
     * 分派事件逻辑
     *
     * @param eventType 事件类型
     * @param event     事件实例
     */
    private void doPost(Class<?> eventType, Object event) {
        EventHandler eventHandler = event2Handler.get(eventType);
        if (eventHandler != null) {
            try {
                EventHandler.handleEvent(eventHandler, this, event);
            } catch (Exception e) {
                log.error("", e);
            }
        } else {
            throw new IllegalStateException("can not find event handler to handle event " + eventType);
        }
    }

    @Override
    public void post(Object event) {
        if (isStopped()) {
            throw new IllegalStateException("event bus is stopped");
        }

        doPost(event.getClass(), event);
    }

    @Override
    public Future<?> schedule(Object event, long delay, TimeUnit unit) {
        if (isStopped()) {
            throw new IllegalStateException("event bus is stopped");
        }
        return scheduler.schedule(() -> post(event), delay, unit);

    }

    @Override
    public Future<?> scheduleAtFixRate(Object event, long initialDelay, long period, TimeUnit unit) {
        if (isStopped()) {
            throw new IllegalStateException("event bus is stopped");
        }
        return scheduler.scheduleAtFixedRate(() -> post(event), initialDelay, period, unit);

    }

    @Override
    public Future<?> scheduleWithFixedDelay(Object event, long initialDelay, long delay, TimeUnit unit) {
        if (isStopped()) {
            throw new IllegalStateException("event bus is stopped");
        }
        return scheduler.scheduleWithFixedDelay(() -> post(event), initialDelay, delay, unit);

    }

    @Override
    public void post(Runnable task) {
        if (isStopped()) {
            throw new IllegalStateException("event bus is stopped");
        }

        task.run();
    }

    @Override
    public void shutdown() {
        if (isStopped()) {
            throw new IllegalStateException("event bus is stopped");
        }

        stopped = true;
        for (EventHandler<?> eventHandler : event2Handler.values()) {
            eventHandler.close();
        }
        event2Handler.clear();
        scheduler.shutdown();
    }

    @Override
    public void close() {
        shutdown();
    }

    //getter
    public boolean isStopped() {
        return stopped;
    }

    ExecutionContext getScheduler() {
        return scheduler;
    }
}
