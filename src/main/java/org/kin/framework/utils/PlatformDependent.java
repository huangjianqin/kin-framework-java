package org.kin.framework.utils;

import org.jctools.queues.MpscArrayQueue;
import org.jctools.queues.MpscChunkedArrayQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jctools.queues.SpscLinkedQueue;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;
import org.jctools.queues.atomic.MpscChunkedAtomicArrayQueue;
import org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;
import org.jctools.queues.atomic.SpscLinkedAtomicQueue;
import org.jctools.util.Pow2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * Forked from <a href="https://github.com/netty/netty">Netty</a>.
 *
 * @author huangjianqin
 * @date 2022/4/6
 */
public final class PlatformDependent {
    private static final Logger log = LoggerFactory.getLogger(PlatformDependent.class);

    /** 默认chunk size */
    private static final int MPSC_CHUNK_SIZE =  1024;
    /** mpsc最小容量 */
    private static final int MIN_MAX_MPSC_CAPACITY =  MPSC_CHUNK_SIZE * 2;
    /** mpsc允许的最大容量, {@link Integer#MAX_VALUE} / 2 */
    private static final int MAX_ALLOWED_MPSC_CAPACITY = Pow2.MAX_POW2;

    /**
     * 构造无界伸缩队列(只会扩容)
     */
    public static <T> Queue<T> newMpscQueue() {
        return Mpsc.newMpscQueue();
    }

    /**
     * 构造有界伸缩队列, 支持扩容, 也支持当queue容量收缩回去, 可以释放未使用的chunk
     */
    public static <T> Queue<T> newMpscQueue(final int maxCapacity) {
        return Mpsc.newMpscQueue(maxCapacity);
    }

    /**
     * 构造有界伸缩队列, 支持扩容, 也支持当queue容量收缩回去, 可以释放未使用的chunk
     */
    public static <T> Queue<T> newMpscQueue(final int chunkSize, final int maxCapacity) {
        return Mpsc.newChunkedMpscQueue(chunkSize, maxCapacity);
    }

    /**
     * 构造spsc队列
     */
    public static <T> Queue<T> newSpscQueue() {
        return UnsafeUtil.hasUnsafe() ? new SpscLinkedQueue<T>() : new SpscLinkedAtomicQueue<T>();
    }

    /**
     * 构造固定容量的mpsc队列
     */
    public static <T> Queue<T> newFixedMpscQueue(int capacity) {
        return UnsafeUtil.hasUnsafe() ? new MpscArrayQueue<T>(capacity) : new MpscAtomicArrayQueue<T>(capacity);
    }

    /**
     * 构造无锁mpsc队列工具方法
     */
    private static final class Mpsc {
        /** 是否使用MpscChunkedArrayQueue */
        private static final boolean USE_MPSC_CHUNKED_ARRAY_QUEUE;

        private Mpsc() {
        }

        static {
            if (UnsafeUtil.hasUnsafe()) {
                log.debug("org.jctools-core.MpscChunkedArrayQueue: unavailable");
                USE_MPSC_CHUNKED_ARRAY_QUEUE = false;
            } else {
                log.debug("org.jctools-core.MpscChunkedArrayQueue: available");
                USE_MPSC_CHUNKED_ARRAY_QUEUE = true;
            }
        }

        /**
         * 构造有界伸缩队列, 支持扩容, 也支持当queue容量收缩回去, 可以释放未使用的chunk
         */
        static <T> Queue<T> newMpscQueue(final int maxCapacity) {
            // Calculate the max capacity which can not be bigger than MAX_ALLOWED_MPSC_CAPACITY.
            // This is forced by the MpscChunkedArrayQueue implementation as will try to round it
            // up to the next power of two and so will overflow otherwise.
            final int capacity = Math.max( Math.min(maxCapacity, MAX_ALLOWED_MPSC_CAPACITY), MIN_MAX_MPSC_CAPACITY);
            return newChunkedMpscQueue(MPSC_CHUNK_SIZE, capacity);
        }

        /**
         * 构造无界伸缩队列(只会扩容)
         */
        static <T> Queue<T> newMpscQueue() {
            return USE_MPSC_CHUNKED_ARRAY_QUEUE ? new MpscUnboundedArrayQueue<T>(MPSC_CHUNK_SIZE)
                    : new MpscUnboundedAtomicArrayQueue<T>(MPSC_CHUNK_SIZE);
        }

        /**
         * 构造有界伸缩队列, 支持扩容, 也支持当queue容量收缩回去, 可以释放未使用的chunk
         */
        static <T> Queue<T> newChunkedMpscQueue(final int chunkSize, final int capacity) {
            return USE_MPSC_CHUNKED_ARRAY_QUEUE ? new MpscChunkedArrayQueue<T>(chunkSize, capacity)
                    : new MpscChunkedAtomicArrayQueue<T>(chunkSize, capacity);
        }
    }
}
