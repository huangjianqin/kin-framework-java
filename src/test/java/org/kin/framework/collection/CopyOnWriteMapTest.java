package org.kin.framework.collection;

import org.kin.framework.concurrent.ExecutionContext;

import java.util.concurrent.CountDownLatch;

/**
 * @author huangjianqin
 * @date 2022/4/15
 */
public class CopyOnWriteMapTest {
    public static void main(String[] args) throws InterruptedException {
        int num = 5;
        CopyOnWriteMap<Integer, Integer> copyOnWriteMap = new CopyOnWriteMap<>();

        for (int i = 0; i < num; i++) {
            copyOnWriteMap.put(i, 0);
        }
        System.out.println(copyOnWriteMap);


        int executeNum = num * 2;
        CountDownLatch latch = new CountDownLatch(executeNum);
        ExecutionContext executionContext = ExecutionContext.cache("worker");
        for (int i = 0; i < executeNum; i++) {
            executionContext.execute(() -> {
                for (int j = 0; j < 100000; j++) {
                    int key = j % 5;
                    copyOnWriteMap.merge(key, 1, Integer::sum);
                }
                latch.countDown();
            });
        }

        latch.await();
        executionContext.shutdown();
        System.out.println(copyOnWriteMap);
    }
}
