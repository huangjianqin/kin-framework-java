package org.kin.framework.collection;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Forked from <a href="https://github.com/netty/netty">Netty</a>.
 * @author huangjianqin
 * @date 2023/6/14
 */
@ThreadSafe
public interface AttributeMap {
    /**
     * get the {@link Attribute} for the given {@link AttributeKey}.
     * This method will never return null, but may return an {@link Attribute} which does not have a value set yet.
     */
    <T> Attribute<T> attr(AttributeKey<T> key);

    /**
     * returns {@code true} if and only if the given {@link Attribute} exists in this {@link AttributeMap}.
     */
    <T> boolean hasAttr(AttributeKey<T> key);

    /**
     * removes this attribute from the {@link AttributeMap} and returns the old attribute value.
     */
    <T> T removeAttr(AttributeKey<T> key);
}
