package org.kin.framework.collection;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * 属性
 * 原子获取和修改value
 * Forked from <a href="https://github.com/netty/netty">Netty</a>.
 * @author huangjianqin
 * @date 2023/6/14
 */
@ThreadSafe
public interface Attribute<T> {
    /**
     * 返回attribute key
     *
     * @return attribute key
     */
    AttributeKey<T> key();

    /**
     * 返回attribute value
     *
     * @return attribute key
     */
    T get();

    /**
     * set attribute value
     */
    void set(T value);

    /**
     * atomically set attribute value and returns the old attribute value
     *
     * @param value new attribute value
     * @return old attribute value, may be null if not exists
     */
    @Nullable
    T getAndSet(T value);

    /**
     * atomically set attribute value if attribute value is not set before
     *
     * @param value attribute value
     * @return attribute value. if the attribute value set before {@link #setIfAbsent} call,
     * it will return the current attribute value, not {@code value}
     */
    T setIfAbsent(T value);

    /**
     * atomically sets attribute value to {@code newValue} if the current attribute value == {@code oldValue}.
     * @return true表示set成功
     */
    boolean compareAndSet(T oldValue, T newValue);
}
