package org.kin.framework.concurrent;

import java.util.Objects;
import java.util.WeakHashMap;

/**
 * 为{@link EventLoop}提供类似于{@link ThreadLocal}的特性
 *
 * @author huangjianqin
 * @date 2023/2/15
 */
public final class EventLoopContext {
    private static final ThreadLocal<EventLoopContext> EVENT_LOOP_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    /** 上下文属性 */
    private final WeakHashMap<Key, Object> kvs = new WeakHashMap<>();

    EventLoopContext() {
    }

    /**
     * 获取线程本地的{@link  EventLoopContext}实例
     */
    static EventLoopContext current() {
        EventLoopContext eventLoopContext = EVENT_LOOP_CONTEXT_THREAD_LOCAL.get();
        if (Objects.isNull(eventLoopContext)) {
            //init
            eventLoopContext = new EventLoopContext();
            update(eventLoopContext);
        }
        return eventLoopContext;
    }

    /**
     * 更新线程本地的{@link  EventLoopContext}实例
     */
    static void update(EventLoopContext context) {
        EVENT_LOOP_CONTEXT_THREAD_LOCAL.set(context);
    }

    /**
     * 移除线程本地的{@link  EventLoopContext}实例
     */
    static void remove() {
        EVENT_LOOP_CONTEXT_THREAD_LOCAL.remove();
    }

    /**
     * 获取上下文属性
     *
     * @param key 属性key
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Key key) {
        EventLoopContext eventLoopContext = current();
        return (T) eventLoopContext.kvs.get(key);
    }

    /**
     * 获取上下文属性, 如果不存在, 则返回默认值
     *
     * @param key          属性key
     * @param defaultValue 属性默认值
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrDefault(Key key, Object defaultValue) {
        EventLoopContext eventLoopContext = current();
        return (T) eventLoopContext.kvs.getOrDefault(key, defaultValue);
    }

    /**
     * 更新上下文属性
     *
     * @param key   属性key
     * @param value 属性值
     * @return 原属性值
     */
    @SuppressWarnings("unchecked")
    public static <T> T put(Key key, Object value) {
        EventLoopContext eventLoopContext = current();
        return (T) eventLoopContext.kvs.put(key, value);
    }

    /**
     * 移除上下文属性
     *
     * @param key 属性key
     * @return 原属性值
     */
    @SuppressWarnings("unchecked")
    public static <T> T remove(Key key) {
        EventLoopContext eventLoopContext = current();
        return (T) eventLoopContext.kvs.remove(key);
    }

    /**
     * 属性key
     */
    public static final class Key {
        private final String name;

        public static Key of(String name) {
            return new Key(name);
        }

        private Key(String name) {
            this.name = name;
        }

        //getter
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }
            Key key = (Key) o;
            return Objects.equals(name, key.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
