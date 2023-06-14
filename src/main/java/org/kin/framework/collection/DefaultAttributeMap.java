package org.kin.framework.collection;

import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * {@link AttributeMap}默认实现
 * use simple synchronization per bucket to keep the memory overhead as low as possible
 * 算法与{@link java.util.concurrent.ConcurrentHashMap}类似, 但更节省内存消耗
 *
 * Forked from <a href="https://github.com/netty/netty">Netty</a>.
 * @author huangjianqin
 * @date 2023/6/14
 */
public class DefaultAttributeMap  implements AttributeMap {
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<DefaultAttributeMap, AtomicReferenceArray> updater =
            AtomicReferenceFieldUpdater.newUpdater(DefaultAttributeMap.class, AtomicReferenceArray.class, "attributes");
    /** 原子数组bucket数量 */
    private static final int BUCKET_SIZE = 4;
    /** bucket mask */
    private static final int MASK = BUCKET_SIZE  - 1;

    /** bucket数组, lazy init */
    @SuppressWarnings("UnusedDeclaration")
    private volatile AtomicReferenceArray<DefaultAttribute<?>> attributes;

    @SuppressWarnings("unchecked")
    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        Preconditions.checkNotNull(key, "key");
        AtomicReferenceArray<DefaultAttribute<?>> attributes = this.attributes;
        if (attributes == null) {
            // Not using ConcurrentHashMap due to high memory consumption.
            attributes = new AtomicReferenceArray<>(BUCKET_SIZE);

            if (!updater.compareAndSet(this, null, attributes)) {
                attributes = this.attributes;
            }
        }

        int i = index(key);
        DefaultAttribute<?> head = attributes.get(i);
        if (head == null) {
            // No head exists yet which means we may be able to add the attribute without synchronization and just
            // use compare and set. At worst we need to fallback to synchronization and waste two allocations.
            head = new DefaultAttribute<>();
            DefaultAttribute<T> attr = new DefaultAttribute<T>(head, key);
            head.next = attr;
            attr.prev = head;
            if (attributes.compareAndSet(i, null, head)) {
                // we were able to add it so return the attr right away
                return attr;
            } else {
                head = attributes.get(i);
            }
        }

        synchronized (head) {
            DefaultAttribute<?> curr = head;
            for (;;) {
                DefaultAttribute<?> next = curr.next;
                if (next == null) {
                    DefaultAttribute<T> attr = new DefaultAttribute<T>(head, key);
                    curr.next = attr;
                    attr.prev = curr;
                    return attr;
                }

                if (next.key == key && !next.removed) {
                    return (Attribute<T>) next;
                }
                curr = next;
            }
        }
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        Preconditions.checkNotNull(key, "key");
        AtomicReferenceArray<DefaultAttribute<?>> attributes = this.attributes;
        if (attributes == null) {
            // no attribute exists
            return false;
        }

        int i = index(key);
        DefaultAttribute<?> head = attributes.get(i);
        if (head == null) {
            // No attribute exists which point to the bucket in which the head should be located
            return false;
        }

        // We need to synchronize on the head.
        synchronized (head) {
            // Start with head.next as the head itself does not store an attribute.
            DefaultAttribute<?> curr = head.next;
            while (curr != null) {
                if (curr.key == key && !curr.removed) {
                    return true;
                }
                curr = curr.next;
            }
            return false;
        }
    }

    @Override
    public <T> T removeAttr(AttributeKey<T> key) {
        return (T) ((DefaultAttribute<T>)attr(key)).getAndRemove();
    }

    /**
     * 计算bucket数组index
     * @param key   attribute key
     * @return  bucket数组index
     */
    private static int index(AttributeKey<?> key) {
        return key.id() & MASK;
    }

    //---------------------------------------------------------------------------------------------------------
    /**
     * {@link Attribute}默认实现
     * Forked from <a href="https://github.com/netty/netty">Netty</a>.
     * @author huangjianqin
     * @date 2023/6/14
     */
    private static class DefaultAttribute<T> extends AtomicReference<T> implements Attribute<T> {
        /** 所在链表head */
        private final DefaultAttribute<?> head;
        /** 关联的attribute key */
        private final AttributeKey<T> key;

        /** prev linked list node */
        private DefaultAttribute<?> prev;
        /** next linked list node */
        private DefaultAttribute<?> next;

        /** 标识attribute是否已被移除 */
        private volatile boolean removed;

        DefaultAttribute(DefaultAttribute<?> head, AttributeKey<T> key) {
            this.head = head;
            this.key = key;
        }

        // Special constructor for the head of the linked-list.
        DefaultAttribute() {
            head = this;
            key = null;
        }

        @Override
        public AttributeKey<T> key() {
            return key;
        }

        @Override
        public T setIfAbsent(T value) {
            while (!compareAndSet(null, value)) {
                T old = get();
                if (old != null) {
                    return old;
                }
            }
            return null;
        }

        /**
         * removes this attribute from the {@link AttributeMap} and returns the old attribute value.
         *
         * If you only want to return the old attribute value and clear the {@link Attribute} while still keep it in the
         * {@link AttributeMap} use {@link #getAndSet(Object)} with a value of {@code null}.
         *
         * <p>
         * Be aware that even if you call this method another thread that has obtained a reference to this {@link Attribute}
         * via {@link AttributeMap#attr(AttributeKey)} will still operate on the same instance. That said if now another
         * thread or even the same thread later will call {@link AttributeMap#attr(AttributeKey)} again, a new
         * {@link Attribute} instance is created and so is not the same as the previous one that was removed. Because of
         * this special caution should be taken when you call {@link #remove()} or {@link #getAndRemove()}.
         */
        private T getAndRemove() {
            removed = true;
            T oldValue = getAndSet(null);
            remove0();
            return oldValue;
        }

        /**
         * removes this attribute from the {@link AttributeMap}
         *
         * If you only want to clear the attribute value and clear the {@link Attribute} while still keep it in
         * {@link AttributeMap} use {@link #set(Object)} with a value of {@code null}.
         *
         * <p>
         * Be aware that even if you call this method another thread that has obtained a reference to this {@link Attribute}
         * via {@link AttributeMap#attr(AttributeKey)} will still operate on the same instance. That said if now another
         * thread or even the same thread later will call {@link AttributeMap#attr(AttributeKey)} again, a new
         * {@link Attribute} instance is created and so is not the same as the previous one that was removed. Because of
         * this special caution should be taken when you call {@link #remove()} or {@link #getAndRemove()}.
         *
         * @deprecated please consider using {@link #set(Object)} (with value of {@code null}).
         */
        private void remove() {
            removed = true;
            set(null);
            remove0();
        }

        private void remove0() {
            synchronized (head) {
                if (prev == null) {
                    // Removed before.
                    return;
                }

                prev.next = next;

                if (next != null) {
                    next.prev = prev;
                }

                // Null out prev and next - this will guard against multiple remove0() calls which may corrupt
                // the linked list for the bucket.
                prev = null;
                next = null;
            }
        }
    }
}
