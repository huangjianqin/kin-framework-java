package org.kin.framework.concurrent;

import com.google.common.annotations.VisibleForTesting;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 解决{@link LinkedBlockingQueue} OOM问题的{@link LinkedBlockingQueue}实现
 *
 * @author huangjianqin
 * @date 2022/9/6
 */
public final class MemorySafeLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {
    private static final long serialVersionUID = -2595273993098111718L;

    /** 256MB */
    private static final int THE_256_MB = 256 * 1024 * 1024;
    /** 最大可用内存限制, 可用内存低于该数值, 则queue不可插入 */
    private int freeMemoryLimit;
    /** 入队queue失败(剩余内存不足)时, 拒绝入队逻辑 */
    private final QueueMemLimitedRejector<E> rejector;

    public MemorySafeLinkedBlockingQueue() {
        this(THE_256_MB);
    }

    public MemorySafeLinkedBlockingQueue(final int freeMemoryLimit) {
        this(freeMemoryLimit, new QueueMemLimitedRejector.DiscardPolicy<>());
    }

    public MemorySafeLinkedBlockingQueue(final int freeMemoryLimit, final QueueMemLimitedRejector<E> rejector) {
        super(Integer.MAX_VALUE);
        this.freeMemoryLimit = freeMemoryLimit;
        this.rejector = rejector;
    }

    public MemorySafeLinkedBlockingQueue(final Collection<? extends E> c) {
        this(c, THE_256_MB);
    }

    public MemorySafeLinkedBlockingQueue(final Collection<? extends E> c, final int freeMemoryLimit) {
        this(c, freeMemoryLimit, new QueueMemLimitedRejector.DiscardPolicy<>());
    }

    public MemorySafeLinkedBlockingQueue(final Collection<? extends E> c, final int freeMemoryLimit, final QueueMemLimitedRejector<E> rejector) {
        super(c);
        this.freeMemoryLimit = freeMemoryLimit;
        this.rejector = rejector;
    }

    @Override
    public void put(final E e) throws InterruptedException {
        if (hasRemainedMemory()) {
            super.put(e);
        } else {
            rejector.reject(e, this);
        }
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        if (!hasRemainedMemory()) {
            rejector.reject(e, this);
            return false;
        }
        return super.offer(e, timeout, unit);
    }

    @Override
    public boolean offer(final E e) {
        if (!hasRemainedMemory()) {
            rejector.reject(e, this);
            return false;
        }
        return super.offer(e);
    }

    /**
     * 返回剩余可用内存是否足够
     *
     * @return 剩余可用内存是否足够
     */
    public boolean hasRemainedMemory() {
        return MemoryCalculator.maxAvailable() > freeMemoryLimit;
    }

    //setter && getter
    @VisibleForTesting
    void setFreeMemoryLimit(int freeMemoryLimit) {
        this.freeMemoryLimit = freeMemoryLimit;
    }

    public int getFreeMemoryLimit() {
        return freeMemoryLimit;
    }

    public QueueMemLimitedRejector<E> getRejector() {
        return rejector;
    }
}
