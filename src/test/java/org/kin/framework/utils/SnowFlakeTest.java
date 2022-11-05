package org.kin.framework.utils;

import com.google.common.base.Stopwatch;
import org.checkerframework.checker.units.qual.C;

import java.util.concurrent.*;

/**
 * @author huangjianqin
 * @date 2022/11/5
 */
public class SnowFlakeTest {
    private static final ConcurrentSkipListSet<Long> IDS = new ConcurrentSkipListSet<>();

    public static void main(String[] args) throws InterruptedException {
        SnowFlake snowFlake = new SnowFlake(1, 1);
        int times = 10_000_000;
        Stopwatch watcher = Stopwatch.createStarted();
        CountDownLatch latch = new CountDownLatch(times);
        for (int i = 0; i < times; i++) {
            ForkJoinPool.commonPool().execute(() -> {
                try {
                    if (!IDS.add(snowFlake.nextId())) {
                        System.err.println("ID生成冲突!!");
                        System.out.println(IDS);
                        System.exit(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(IDS);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        watcher.stop();
        System.out.printf("end, total run %d ms!!\r\n", watcher.elapsed(TimeUnit.MILLISECONDS));
    }
}
