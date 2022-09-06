package org.kin.framework.concurrent;

/**
 * @author huangjianqin
 * @date 2022/9/6
 */
public class MemorySafeLinkedBlockingQueueTest {
    public static void main(String[] args) {
        MemorySafeLinkedBlockingQueue<Integer> queue = new MemorySafeLinkedBlockingQueue<>((int) MemoryCalculator.calculate(0.9), new QueueMemLimitedRejector.AbortPolicy<>());
        int count = 0;
        try {
            while (true) {
                count++;
                queue.add(new Integer(111));
            }
        }finally {
            System.out.println(count);
        }
    }
}
