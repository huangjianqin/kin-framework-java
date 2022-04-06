/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kin.framework.pool;

import org.jctools.queues.MessagePassingQueue;
import org.kin.framework.concurrent.FastThreadLocal;
import org.kin.framework.utils.PlatformDependent;
import org.kin.framework.utils.SysUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Light-weight object pool.
 * <p>
 * Forked from <a href="https://github.com/netty/netty">Netty</a>.
 *
 * @param <T> the type of the pooled object
 * @author huangjianqin
 * @date 2021/11/3
 */
public abstract class Recycler<T> {
    private static final Logger log = LoggerFactory.getLogger(Recycler.class);

    /** 最大池化对象数 */
    public static final int MAX_CAPACITY_PER_THREAD;
    /** 池化比率, 每ratio个对象中只有一个会被池化, 默认是8 */
    public static final int RATIO;
    /** 默认mpsc无锁队列的chunk size */
    public static final int QUEUE_CHUNK_SIZE_PER_THREAD;

    static {
        //初始化系统属性变量
        int defaultInitialMaxCapacityPerThread = 4 * 1024;
        int maxCapacityPerThread = SysUtils.getIntSysProperty("kin.framework.recycler.maxCapacityPerThread", defaultInitialMaxCapacityPerThread);
        if (maxCapacityPerThread < 0) {
            maxCapacityPerThread = defaultInitialMaxCapacityPerThread;
        }

        // By default we allow one push to a Recycler for each 8th try on handles that were never recycled before.
        // This should help to slowly increase the capacity of the recycler while not be too sensitive to allocation
        // bursts.
        MAX_CAPACITY_PER_THREAD = maxCapacityPerThread;
        RATIO = max(0, SysUtils.getIntSysProperty("kin.framework.recycler.ratio", 8));
        QUEUE_CHUNK_SIZE_PER_THREAD = SysUtils.getIntSysProperty("kin.framework.recycler.chunkSize", 32);

        if (log.isDebugEnabled()) {
            log.debug("-Dkin.framework.maxCapacityPerThread: {}.", MAX_CAPACITY_PER_THREAD == 0 ? "disabled" : MAX_CAPACITY_PER_THREAD);
            log.debug("-Dkin.framework.recycler.ratio: {}.", RATIO == 0 ? "disabled" : RATIO);
            log.debug("-Dkin.framework.recycler.chunkSize: {}.", QUEUE_CHUNK_SIZE_PER_THREAD == 0 ? "disabled" : QUEUE_CHUNK_SIZE_PER_THREAD);
        }
    }

    /** 每个线程最大对象数 */
    private final int maxCapacityPerThread;
    /** 池化比率 */
    private final int ratio;
    /** mpsc无锁队列的chunk size */
    private final int chunkSize;
    /** 本线程绑定的{@link LocalPool}实例 */
    private final FastThreadLocal<LocalPool<T>> threadLocal = new FastThreadLocal<LocalPool<T>>() {
        @Override
        protected LocalPool<T> initialValue() {
            return new LocalPool<>(maxCapacityPerThread, ratio, chunkSize);
        }

        @Override
        protected void onRemoval(LocalPool<T> value) throws Exception {
            super.onRemoval(value);
            MessagePassingQueue<DefaultHandle<T>> handles = value.pooledHandles;
            //help gc
            value.pooledHandles = null;
            //释放mpsc队列内存
            handles.clear();
        }
    };

    protected Recycler() {
        this(MAX_CAPACITY_PER_THREAD);
    }

    protected Recycler(int maxCapacityPerThread) {
        this(maxCapacityPerThread, RATIO, QUEUE_CHUNK_SIZE_PER_THREAD);
    }

    protected Recycler(int maxCapacityPerThread, int ratio, int chunkSize) {
        this.ratio = max(0, ratio);
        if (maxCapacityPerThread <= 0) {
            this.maxCapacityPerThread = 0;
            this.chunkSize = 0;
        } else {
            //最小为4
            this.maxCapacityPerThread = max(4, maxCapacityPerThread);
            //最小为2, 默认maxCapacityPerThread*2, 但也不能超过chunkSize
            this.chunkSize = max(2, min(chunkSize, this.maxCapacityPerThread >> 1));
        }
    }

    /**
     * 获取池化对象
     */
    @SuppressWarnings("unchecked")
    public final T get() {
        if (maxCapacityPerThread == 0) {
            //不使用池化
            return newObject((ObjectPool.Handle<T>) ObjectPool.NOOP_HANDLE);
        }
        //获取handle pool
        LocalPool<T> localPool = threadLocal.get();
        //复用handle
        DefaultHandle<T> handle = localPool.claim();
        T obj;
        if (handle == null) {
            //没有可复用handle, 尝试new一个
            handle = localPool.newHandle();
            if (handle != null) {
                //new成功, 池化该对象
                obj = newObject(handle);
                handle.set(obj);
            } else {
                //new失败, 不池化
                obj = newObject((ObjectPool.Handle<T>) ObjectPool.NOOP_HANDLE);
            }
        } else {
            //有可复用handle, 返回其池化对象
            obj = handle.get();
        }

        return obj;
    }

    /**
     * 实现类自定的创建池化对象方法
     */
    protected abstract T newObject(ObjectPool.Handle<T> handle);

    /**
     * @return  获取本线程当前可用池化对象数量
     */
    public final int threadLocalSize() {
        return threadLocal.get().pooledHandles.size();
    }

    //-------------------------------------------

    /**
     * 池化对象绑定的{@link org.kin.framework.pool.ObjectPool.Handle}(句柄). handle缓存了需要池化的对象
     */
    private static final class DefaultHandle<T> implements ObjectPool.Handle<T> {
        /** 状态-被占用中 */
        private static final int STATE_CLAIMED = 0;
        /** 状态-可用 */
        private static final int STATE_AVAILABLE = 1;
        /** cas update */
        private static final AtomicIntegerFieldUpdater<DefaultHandle<?>> STATE_UPDATER;

        static {
            AtomicIntegerFieldUpdater<?> updater = AtomicIntegerFieldUpdater.newUpdater(DefaultHandle.class, "state");
            //noinspection unchecked
            STATE_UPDATER = (AtomicIntegerFieldUpdater<DefaultHandle<?>>) updater;
        }

        /**
         * 由{@link #STATE_UPDATER}更新
         * new出来的{@link DefaultHandle}的state都等于{@link #STATE_CLAIMED}, 即被占用(没有人会创建了却不使用), 后面可以被release
         */
        @SuppressWarnings({"FieldMayBeFinal", "unused"})
        private volatile int state;
        /** 绑定的线程本地handle pool */
        private final LocalPool<T> localPool;
        /** 被池化的对象 */
        private T value;

        DefaultHandle(LocalPool<T> localPool) {
            this.localPool = localPool;
        }

        @Override
        public void recycle(T self) {
            if (self != value) {
                throw new IllegalArgumentException("object does not belong to handle");
            }
            //释放, 放入mpsc队列, 等待被复用
            localPool.release(this);
        }

        /**
         * 获取池化对象
         */
        T get() {
            return value;
        }

        /**
         * 更新池化对象
         */
        void set(T value) {
            this.value = value;
        }

        /**
         * cas将状态由{@link #STATE_AVAILABLE}设置为{@link #STATE_CLAIMED}
         */
        boolean availableToClaim() {
            if (state != STATE_AVAILABLE) {
                return false;
            }
            return STATE_UPDATER.compareAndSet(this, STATE_AVAILABLE, STATE_CLAIMED);
        }

        /**
         * cas将状态设置为{@link #STATE_AVAILABLE}
         */
        void toAvailable() {
            int prev = STATE_UPDATER.getAndSet(this, STATE_AVAILABLE);
            if (prev == STATE_AVAILABLE) {
                throw new IllegalStateException("Object has been recycled already.");
            }
        }
    }

    /**
     * 线程绑定available handle pool
     */
    private static final class LocalPool<T> {
        /** 池化比率 */
        private final int ratio;
        /** mpsc伸缩无锁队列 */
        private volatile MessagePassingQueue<DefaultHandle<T>> pooledHandles;
        /** 池化对象计数器 */
        private int ratioCounter;

        @SuppressWarnings("unchecked")
        LocalPool(int maxCapacity, int ratio, int chunkSize) {
            this.ratio = ratio;
            pooledHandles = (MessagePassingQueue<DefaultHandle<T>>) PlatformDependent.newMpscQueue(chunkSize, maxCapacity);
            //这样做的目的是第一个对象就会被池化复用
            ratioCounter = ratio;
        }

        /**
         * 申请handle, 申请成功, 则标识对象可以被池化复用
         */
        DefaultHandle<T> claim() {
            MessagePassingQueue<DefaultHandle<T>> handles = pooledHandles;
            if (handles == null) {
                //pool removed
                return null;
            }

            DefaultHandle<T> handle;
            do {
                //从队列里面取可复用的handle
                handle = handles.relaxedPoll();
            } while (handle != null && !handle.availableToClaim()); //cas修改状态成功
            return handle;
        }

        /**
         * 释放handle, 留给后面新对象使用
         */
        void release(DefaultHandle<T> handle) {
            MessagePassingQueue<DefaultHandle<T>> handles = pooledHandles;
            //cas修改状态
            handle.toAvailable();
            if (handles != null) {
                //入队, 等待被复用
                handles.relaxedOffer(handle);
            }
        }

        /**
         * 构造新handle
         */
        DefaultHandle<T> newHandle() {
            if (++ratioCounter >= ratio) {
                //每ratio次调用, 返回一次DefaultHandle新实例, 即1/ratio
                ratioCounter = 0;
                return new DefaultHandle<>(this);
            }
            return null;
        }
    }
}
