package org.kin.framework.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author huangjianqin
 * @date 2023/8/18
 */
public class PeakEWMATest {
    public static void main(String[] args) throws InterruptedException {
        PeakEWMA ewma = null;
        int loop = 1000;
        for (int i = 0; i < loop; i++) {
            //正常在800-1200之间波动
            int elapsed = 800 + ThreadLocalRandom.current().nextInt(400);
            if (i % 5 == 0) {
                //模拟突然峰值
                elapsed += ThreadLocalRandom.current().nextInt(1_000);
            }
            Thread.sleep(elapsed);

            if (ewma == null) {
                ewma = new PeakEWMA(1_000, elapsed);
            } else {
                ewma.observe(elapsed);
            }

            System.out.println(elapsed);
            System.out.println(ewma.getEwma());
            System.out.println("-----------------------------");
        }
    }
}
