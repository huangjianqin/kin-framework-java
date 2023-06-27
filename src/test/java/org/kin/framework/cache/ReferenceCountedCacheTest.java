package org.kin.framework.cache;

import java.util.concurrent.*;

/**
 * @author huangjianqin
 * @date 2023/6/27
 */
public class ReferenceCountedCacheTest {
    public static void main(String[] args) throws InterruptedException {
        ReferenceCountedCache<String, String> cache = new ReferenceCountedCache<>((k, v) -> System.out.println(String.format("%s-%s removed", k, v)));
        String k1 = "k1";
        String k2 = "k2";

        System.out.println(cache.get(k1, () -> new String("abc")));
        System.out.println(cache.get(k1, () -> new String("abc")) == cache.get(k1, () -> new String("abc")));
        cache.release(k1);
        cache.release(k1);
        cache.release(k1);
        System.out.println("-----------------------------------");

        String efg1 = cache.get(k2, () -> new String("efg"));
        System.out.println(efg1);
        cache.release(k2);
        String efg2 = cache.get(k2, () -> new String("efg"));
        System.out.println(efg2);
        System.out.println(efg1 == efg2);
        cache.get(k2, () -> new String("efg"));
        //refcnt=2
        System.out.println(cache);
        cache.remove(k2);
        System.out.println("-----------------------------------");

        ExecutorService executor = Executors.newFixedThreadPool(200);
        try {
            int num = 2000;
            CountDownLatch latch = new CountDownLatch(num);
            for (int i = num; i > 0; i--) {
                int rand = 10 + ThreadLocalRandom.current().nextInt(500);
                String key = "k" + rand;
                String value = "cv" + rand;
                executor.execute(() -> {
                    for (int j = 0; j < 1000; j++) {
                        cache.get(key, () -> new String(value));
                        try {
                            Thread.sleep(ThreadLocalRandom.current().nextInt(200));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        cache.release(key);
                        try {
                            Thread.sleep(ThreadLocalRandom.current().nextInt(200));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        if (j > 0 && j % 500 == 0) {
                            System.out.println(j + "---" + cache);
                        }
                    }
                    latch.countDown();
                });
            }
            latch.await();
            System.out.println(cache);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();

            System.gc();
        }
        System.out.println("cache size: " + cache.size());
        System.out.println("-----------------------------------");

        for (int i = 100; i > 0; i--) {
            int rand = 10 + ThreadLocalRandom.current().nextInt(10);
            String key = "k" + rand;
            String value = "cv" + rand;
            cache.get(key, () -> new String(value));
        }
        System.out.println(cache);
        cache.clear();
        System.out.println(cache);
        System.out.println("-----------------------------------");

        int num = 1000;
        CountDownLatch latch1 = new CountDownLatch(num);
        for (int i = 0; i < num; i++) {
            ForkJoinPool.commonPool().execute(() -> {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(50));
                    cache.get("t", () -> {
                        throw new RuntimeException();
                    });
                } catch (Exception e) {

                }
                latch1.countDown();
            });
        }
        latch1.await();
        System.out.println(cache);
        System.out.println("-----------------------------------");
    }
}
