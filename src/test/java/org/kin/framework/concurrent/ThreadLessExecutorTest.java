package org.kin.framework.concurrent;

import java.util.concurrent.CompletableFuture;

/**
 * @author huangjianqin
 * @date 2022/5/20
 */
public class ThreadLessExecutorTest {
    public static void main(String[] args) throws InterruptedException {
        ThreadLessExecutor executor = new ThreadLessExecutor();

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            executor.execute(()->{throw new RuntimeException("test" + finalI);});
        }

        CompletableFuture<Object> stubFuture = new CompletableFuture<>();
        executor.setWaitingFuture(stubFuture);

        executor.waitAndDrain();

        executor.execute(()->{});

        executor.waitAndDrain();

        executor.shutdown();
    }
}
