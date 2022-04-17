package org.kin.framework.csp;

import org.kin.framework.concurrent.ExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author huangjianqin
 * @date 2022/4/17
 */
public class TpsRuleManagerTest {
    public static void main(String[] args) throws InterruptedException {
        TpsRuleManager tpsRuleManager = TpsRuleManager.instance();
        TpsRuleGroupOptions groupOptions = TpsRuleGroupOptions.builder()
                .group("demo")
                .groupRule(TpsRuleOptions.builder().maxTps(10000).monitorType(TpsMonitorType.INTERCEPT).build())
                .childRuleMap("child1", TpsRuleOptions.builder().maxTps(3).build())
                .childRuleMap("child2", TpsRuleOptions.builder().maxTps(3).build())
                .childRuleMap("child3", TpsRuleOptions.builder().maxTps(3).build())
                .childRuleMap("child*", TpsRuleOptions.builder().maxTps(3).monitorType(TpsMonitorType.INTERCEPT).build())
                .build();
        int num = 10;
        tpsRuleManager.createTpsRule(groupOptions);
        ExecutionContext executionContext = ExecutionContext.fix(num, "worker");
        try{
            CountDownLatch latch = new CountDownLatch(num);
            for (int i = 0; i < num; i++) {
                executionContext.execute(() -> {
                    for (int k = 0; k < 100; k++) {
                        List<String> indexes = new ArrayList<>();
                        int checkNum = ThreadLocalRandom.current().nextInt(4);
                        for (int j = 0; j < checkNum; j++) {
                            indexes.add("child" + (ThreadLocalRandom.current().nextInt(3) + 1));
                        }
                        if (!tpsRuleManager.entry("demo", indexes)) {
                            System.out.println("intercepted");
                            break;
                        }

                        try {
                            Thread.sleep(1_00);
                        } catch (InterruptedException e) {

                        }
                    }
                    latch.countDown();
                });
            }
            latch.await();
        }finally {
            executionContext.shutdown();
            tpsRuleManager.close();
        }
    }
}
