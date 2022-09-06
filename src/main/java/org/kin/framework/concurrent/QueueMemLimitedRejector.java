package org.kin.framework.concurrent;

import java.util.Queue;

/**
 * queue可用内存不足时, 拒绝入队的处理逻辑
 *
 * @author huangjianqin
 * @date 2022/9/6
 * @see AbortPolicy
 * @see DiscardPolicy
 * @see DiscardOldestPolicy
 */
@FunctionalInterface
public interface QueueMemLimitedRejector<E> {
    /**
     * queue可用内存不足时, 拒绝入队的处理逻辑
     */
    void reject(E e, Queue<E> queue);

    /**
     * 抛弃queue第一个item
     *
     * @param <E>
     */
    class DiscardOldestPolicy<E> implements QueueMemLimitedRejector<E> {
        @Override
        public void reject(final E e, final Queue<E> queue) {
            queue.poll();
            queue.offer(e);
        }
    }

    /**
     * 终止执行, 抛出异常
     * @param <E>
     */
    class AbortPolicy<E> implements QueueMemLimitedRejector<E> {

        @Override
        public void reject(final E e, final Queue<E> queue) {
            throw new QueueMemLimitedException("no more memory can be used!");
        }

    }

    /**
     * 抛弃掉入队item
     * @param <E>
     */
    class DiscardPolicy<E> implements QueueMemLimitedRejector<E> {

        @Override
        public void reject(final E e, final Queue<E> queue) {

        }
    }
}
