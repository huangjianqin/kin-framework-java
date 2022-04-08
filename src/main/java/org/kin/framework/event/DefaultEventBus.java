package org.kin.framework.event;

import com.google.common.base.Preconditions;
import org.jctools.maps.NonBlockingHashMap;
import org.kin.framework.concurrent.ExecutionContext;
import org.kin.framework.proxy.MethodDefinition;
import org.kin.framework.proxy.ProxyInvoker;
import org.kin.framework.proxy.Proxys;
import org.kin.framework.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
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
    /** 是否使用字节码增强技术 */
    private final boolean isEnhance;
    /** 调度线程 */
    private final ExecutionContext scheduler;

    private volatile boolean stopped;

    public DefaultEventBus() {
        this(true);
    }

    public DefaultEventBus(boolean isEnhance) {
        this.isEnhance = isEnhance;
        this.scheduler = ExecutionContext.fix(1, "defaultEventBus", 2);
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
     * 从{@link EventHandler}泛型中解析出事件类
     *
     * @param handlerClass {@link EventHandler}实现类
     */
    private Class<?> parseEventType(Class<?> handlerClass) {
        return parseEventRawType(ClassUtils.getSuperInterfacesGenericActualTypes(EventHandler.class, handlerClass).get(0));
    }

    /**
     * 从实际类型中解析出事件类
     *
     * @param type 实际类型
     */
    private Class<?> parseEventRawType(Type type) {
        if (type instanceof ParameterizedType) {
            //@EventFunction注解的方法参数
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> parameterizedRawType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(parameterizedRawType)) {
                //事件合并, 获取集合的泛型
                //以实际事件类型来注册事件处理器
                return (Class<?>) parameterizedType.getActualTypeArguments()[0];
            } else {
                //事件
                return parameterizedRawType;
            }
        } else {
            //EventHandler的泛型
            return (Class<?>) type;
        }
    }

    /**
     * 从{@link EventHandler}实现解析出event class并注册
     */
    private void registerEventHandler(EventHandler eventHandler) {
        Class<?> eventType = parseEventType(eventHandler.getClass());

        registerEventHandler(eventType, eventHandler);
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

        //注解在方法
        //在所有  public & 有注解的  方法中寻找一个匹配的方法作为事件处理方法
        for (Method method : claxx.getMethods()) {
            if (!method.isAnnotationPresent(EventFunction.class)) {
                continue;
            }

            Type[] parameterTypes = method.getGenericParameterTypes();
            int paramNum = parameterTypes.length;
            if (paramNum <= 0 || paramNum > 2) {
                //只处理一个或两个参数的public方法
                continue;
            }

            Class<?> eventType = null;
            //EventBus实现类的方法参数位置, 默认没有
            int eventBusParamIdx = 0;
            for (int i = 1; i <= parameterTypes.length; i++) {
                if (parameterTypes[i - 1] instanceof ParameterizedType) {
                    //泛型参数
                    ParameterizedType parameterizedType = (ParameterizedType) parameterTypes[i - 1];
                    Class<?> parameterizedRawType = (Class<?>) parameterizedType.getRawType();
                    if (EventBus.class.isAssignableFrom(parameterizedRawType)) {
                        eventBusParamIdx = i;
                    } else {
                        eventType = parseEventRawType(parameterizedType);
                    }
                } else {
                    //普通参数
                    Class<?> parameterType = (Class<?>) parameterTypes[i - 1];
                    if (EventBus.class.isAssignableFrom(parameterType)) {
                        eventBusParamIdx = i;
                    } else {
                        eventType = parseEventRawType(parameterType);
                    }
                }
            }

            EventFunction eventFunctionAnno = method.getAnnotation(EventFunction.class);

            registerEventFunc(eventType, generateEventFuncMethodInvoker(obj, method), eventBusParamIdx, eventFunctionAnno.order());
        }
    }

    /**
     * 注册基于{@link EventFunction}注入的事件处理方法
     *
     * @param eventBusParamIdx {@link EventBus}实现类的方法参数位置, 默认0, 标识没有该参数
     */
    private void registerEventFunc(Class<?> eventClass, ProxyInvoker<?> invoker, int eventBusParamIdx, int order) {
        registerEventHandler(eventClass,
                new EventFunctionHandler<>(
                        invoker,
                        eventBusParamIdx,
                        order));
    }

    /**
     * @return {@link EventFunction} 注解方法代理类
     */
    private ProxyInvoker<?> generateEventFuncMethodInvoker(Object obj, Method method) {
        MethodDefinition<Object> methodDefinition = new MethodDefinition<>(obj, method);
        if (isEnhance) {
            return Proxys.byteBuddy().enhanceMethod(methodDefinition);
        } else {
            return Proxys.reflection().enhanceMethod(methodDefinition);
        }
    }

    /**
     * 分派事件逻辑
     *
     * @param eventType 事件类型
     * @param event     事件实例
     */
    private void doPost(Class<?> eventType, Object event) {
        EventHandler handler = event2Handler.get(eventType);
        if (handler != null) {
            try {
                handler.handle(this, event);
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
        return scheduler.schedule(() -> post(event), delay, unit);

    }

    @Override
    public Future<?> scheduleAtFixRate(Object event, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(() -> post(event), initialDelay, period, unit);

    }

    @Override
    public Future<?> scheduleWithFixedDelay(Object event, long initialDelay, long delay, TimeUnit unit) {
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
        event2Handler.clear();
        scheduler.shutdown();
    }

    //getter
    public boolean isStopped() {
        return stopped;
    }
}
