package org.kin.framework.collection;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author huangjianqin
 * @date 2023/6/14
 */
public class DefaultAttributeMapTest {
    public static void main(String[] args) throws InterruptedException {
        AttributeMap attributeMap = new DefaultAttributeMap();
        CountDownLatch latch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            String key = "K" + i;
            ForkJoinPool.commonPool().execute(() -> {
                long l = -1;
                for (int j = 0; j < 100_000_000; j++) {
                    Attribute<Long> attribute = attributeMap.attr(AttributeKey.valueOf(key));
                    l = ThreadLocalRandom.current().nextLong(100000000);
                    attribute.set(l);
                }

                System.out.println(Thread.currentThread() + "---" + key + "---" + l);
                latch.countDown();
            });
        }

        for (int i = 0; i < 5; i++) {
            String key = "K" + i;
            ForkJoinPool.commonPool().execute(() -> {
                for (int j = 0; j < 100_000_000; j++) {
                    Attribute<Long> attribute = attributeMap.attr(AttributeKey.valueOf(key));
                    Long attrValue = attribute.get();
                    if(Objects.nonNull(attrValue)){
                        long v = attrValue * ThreadLocalRandom.current().nextInt(1000);
                    }
                }
            });
        }

        latch.await();

        for (int i = 0; i < 5; i++) {
            String key = "K" + i;
            Attribute<Long> attribute = attributeMap.attr(AttributeKey.valueOf(key));
            System.out.println(key + "---" + attribute.get());
        }
    }
}
