package org.kin.framework.cache;

import com.google.common.base.Preconditions;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.HashUtils;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * value支持引用计数缓存实现
 * 多锁设计
 * 1. 类似ConcurrentHashMap设计. 基于atomic array + linked list, 储存缓存entry, array寻址策略是hashcode(key) % array size. 这样设计的优势是提高访问缓存entry的并发能力
 * 2. 缓存entry的value访问基于entry对象锁, 因为缓存entry具备引用计数, 当引用计数减少到0时, 才会真正释放, 这里需要同步操作才能保证线程安全
 *
 * @author huangjianqin
 * @date 2023/6/27
 */
@ThreadSafe
public class ReferenceCountedCache<K, V> {
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<ReferenceCountedCache, AtomicReferenceArray> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(ReferenceCountedCache.class, AtomicReferenceArray.class, "cache");

    /** 原子数组bucket数量 */
    private final int bucketSize;
    /** bucket mask */
    private final int bucketMask;

    /** bucket数组, lazy init */
    private volatile AtomicReferenceArray<Entry<K, V>> cache;
    /** callback when cache really remove from {@link #cache} */
    private final BiConsumer<K, V> removeListener;

    public ReferenceCountedCache() {
        this(4);
    }

    public ReferenceCountedCache(BiConsumer<K, V> removeListener) {
        this(4, removeListener);
    }

    public ReferenceCountedCache(int bucketSize) {
        this(bucketSize, null);
    }

    public ReferenceCountedCache(int bucketSize, BiConsumer<K, V> removeListener) {
        this.bucketSize = bucketSize;
        this.bucketMask = this.bucketSize - 1;
        this.removeListener = removeListener;
    }

    /**
     * 计算bucket数组index
     *
     * @param k key
     * @return bucket数组index
     */
    private int index(K k) {
        return HashUtils.efficientHash(k, bucketMask);
    }

    /**
     * 添加缓存, 同时增加缓存引用计数
     *
     * @param k 缓存key
     * @param v 缓存value
     */
    public void put(K k, V v) {
        Preconditions.checkNotNull(k, "key");
        get(k, () -> v);
    }

    /**
     * 返回缓存, 如果不存在, 则使用{@code supplier}同步创建value并缓存起来, 同时增加缓存引用计数
     *
     * @param k        缓存key
     * @param supplier value   supplier
     * @return 缓存value
     */
    public V get(K k, Supplier<V> supplier) {
        Preconditions.checkNotNull(k, "key");
        return Objects.requireNonNull(getEntry(k, true)).retainedGetOrCreate(supplier);
    }

    /**
     * 返回缓存, 同时增加缓存引用计数
     *
     * @param k 缓存key
     * @return 缓存value
     */
    @Nullable
    public V get(K k) {
        Preconditions.checkNotNull(k, "key");
        return Objects.requireNonNull(getEntry(k, true)).retainedGet();
    }

    /**
     * 直接返回缓存
     * !!!! 不会增加引用计数
     *
     * @param k 缓存key
     * @return 缓存value
     */
    @Nullable
    public V peek(K k) {
        Preconditions.checkNotNull(k, "key");
        Entry<K, V> entry = getEntry(k, false);
        if (Objects.nonNull(entry)) {
            return entry.getValue();
        } else {
            return null;
        }
    }

    /**
     * 返回缓存entry
     *
     * @param k           缓存key
     * @param newIfAbsent 如果entry不存在, 则创建
     * @return 缓存entry实例
     */
    private Entry<K, V> getEntry(K k, boolean newIfAbsent) {
        AtomicReferenceArray<Entry<K, V>> cache = this.cache;
        if (Objects.isNull(cache)) {
            //lazy init cache
            cache = new AtomicReferenceArray<>(bucketSize);

            if (!UPDATER.compareAndSet(this, null, cache)) {
                //原子更新失败, 说明其他线程初始化cache了, 重新获取
                cache = this.cache;
            }
        }

        int i = index(k);
        Entry<K, V> head = cache.get(i);
        if (Objects.isNull(head)) {
            //lazy init linked list head
            head = new Entry<>();
            if (newIfAbsent) {
                //缺省则创建缓存entry
                //尝试创建head和缓存entry
                Entry<K, V> entry = new Entry<>(head, k);
                head.next = entry;
                entry.prev = head;
                if (cache.compareAndSet(i, null, head)) {
                    return entry;
                } else {
                    head = cache.get(i);
                }
            } else {
                //尝试创建head
                if (!cache.compareAndSet(i, null, head)) {
                    head = cache.get(i);
                }
            }
        }

        synchronized (head) {
            Entry<K, V> curr = head;
            for (; ; ) {
                Entry<K, V> next = curr.next;
                if (next == null) {
                    if (newIfAbsent) {
                        //缺省则创建缓存entry
                        Entry<K, V> attr = new Entry<>(head, k);
                        curr.next = attr;
                        attr.prev = curr;
                        return attr;
                    } else {
                        //直接返回
                        return null;
                    }
                }

                if (next.key.equals(k)) {
                    return next;
                }
                curr = next;
            }
        }
    }

    /**
     * 调用{@link #removeListener}
     *
     * @param entry 缓存entry
     */
    private void applyRemoveListener(K key, V value) {
        if (Objects.isNull(removeListener)) {
            return;
        }

        removeListener.accept(key, value);
    }

    /**
     * 减少该缓存引用计数, 当缓存引用计数<0时, 才会真正从{@link #cache}移除
     *
     * @param k 缓存key
     * @return true表示缓存引用计数减少到0
     */
    public boolean release(K k) {
        Entry<K, V> entry = getEntry(k, false);
        if (Objects.isNull(entry)) {
            return false;
        }

        V value = entry.getValue();
        boolean release = entry.release();

        if (release) {
            applyRemoveListener(k, value);
        }

        return release;
    }

    /**
     * 强制移除缓存, 不管缓存引用计数是多少, 直接从{@link #cache}移除
     *
     * @param k 缓存key
     */
    public void remove(K k) {
        Entry<K, V> entry = getEntry(k, false);
        if (Objects.isNull(entry)) {
            return;
        }

        V value = entry.getValue();
        entry.remove();
        applyRemoveListener(k, value);
    }

    /**
     * 强制清空缓存
     */
    public void clear() {
        AtomicReferenceArray<Entry<K, V>> cache = this.cache;
        if (Objects.isNull(cache)) {
            return;
        }

        List<Entry<K, V>> entries = new ArrayList<>();
        for (int i = 0; i < bucketSize; i++) {
            Entry<K, V> head = cache.get(i);

            while (!cache.compareAndSet(i, head, null)) {
                head = cache.get(i);
            }

            if (Objects.isNull(head)) {
                continue;
            }

            synchronized (head) {
                Entry<K, V> curr = head;
                for (; ; ) {
                    Entry<K, V> next = curr.next;
                    if (Objects.nonNull(next)) {
                        entries.add(next);
                        curr = next;
                    } else {
                        break;
                    }
                }
            }
        }

        for (Entry<K, V> entry : entries) {
            K key = entry.getKey();
            V value = entry.getValue();
            entry.remove();
            applyRemoveListener(key, value);
        }
    }

    /**
     * 返回所有缓存value, 非同步操作, 不保证获取到缓存返回视图
     *
     * @return 集合
     */
    public Collection<V> values() {
        return entries().stream().map(Entry::getValue).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 返回所有缓存entry, 非同步操作, 不保证获取到缓存返回视图
     *
     * @return 缓存entry集合
     */
    private Collection<Entry<K, V>> entries() {
        List<Entry<K, V>> entries = new ArrayList<>();
        AtomicReferenceArray<Entry<K, V>> cache = this.cache;

        if (Objects.isNull(cache)) {
            return Collections.emptyList();
        }

        for (int i = 0; i < bucketSize; i++) {
            Entry<K, V> head = cache.get(i);

            if (Objects.isNull(head)) {
                continue;
            }

            Entry<K, V> curr = head;
            for (; ; ) {
                Entry<K, V> next = curr.next;
                if (Objects.nonNull(next)) {
                    entries.add(next);
                    curr = next;
                } else {
                    break;
                }
            }
        }

        return entries;
    }

    /**
     * 返回当前缓存大小, 非同步操作, 不保证准确
     *
     * @return 缓存大小
     */
    public int size() {
        return entries().size();
    }

    //getter
    @Override
    public String toString() {
        return "ReferenceCountedCache" + entries();
    }

    //----------------------------------------------------------------------------------------------------

    /** cache entry */
    private static class Entry<K, V> {
        /** 所在链表head */
        private final Entry<K, V> head;
        /** cache key */
        private final K key;
        /** cached value */
        private V value;
        /** 计数器 */
        private int counter;

        /** prev linked list node */
        private Entry<K, V> prev;
        /** next linked list node */
        private Entry<K, V> next;

        /**
         * 特殊{@link Entry}实现
         * 用于构造链表head
         */
        private Entry() {
            this(null, null);
        }

        private Entry(Entry<K, V> head, K key) {
            this.head = head;
            this.key = key;
        }

        /**
         * 返回缓存value, 如果不存在则new一个, 同时递增缓存entry引用计数
         *
         * @return this
         */
        public synchronized V retainedGetOrCreate(Supplier<V> supplier) {
            if (value == null) {
                try {
                    value = supplier.get();
                } catch (Exception e) {
                    //异常则移除缓存entry
                    remove();
                    ExceptionUtils.throwExt(e);
                }
            }
            counter++;
            return value;
        }

        /**
         * 返回缓存value, 同时递增缓存entry引用计数
         *
         * @return this
         */
        public synchronized V retainedGet() {
            counter++;
            return value;
        }

        /**
         * 缓存entry引用计数-1. 如果引用计数减少到0, 则会从linked list移除
         *
         * @return true if reference count equal to 0 after desc
         */
        public synchronized boolean release() {
            if (--counter <= 0) {
                //从链表移除
                remove();
                return true;
            } else {
                return false;
            }
        }

        /**
         * 不管缓存entry引用计数是多少, 直接移除
         */
        public synchronized void remove() {
            counter = 0;
            value = null;
            remove0();
        }

        /**
         * 将该entry从链表移除
         */
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

        //getter
        public K getKey() {
            return key;
        }

        public synchronized V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("{k=%s,v={%s,%d}}", key, value, counter);
        }
    }
}
