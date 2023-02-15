package org.kin.framework.concurrent;

/**
 * @author huangjianqin
 * @date 2023/2/15
 */
public class EventLoopContextTest {
    private static final EventLoopContext.Key COUNT_KEY = EventLoopContext.Key.of("count");

    public static void main(String[] args) throws InterruptedException {
        int loopNum = 5;
        ExecutionContext executionContext = ExecutionContext.cache("worker", 1);
        FixOrderedEventLoopGroup pool = new FixOrderedEventLoopGroup(loopNum, executionContext, OrderedEventLoop::new);

        try {
            for (int i = 0; i < 100; i++) {
                pool.next().execute(() -> {
                    int count = EventLoopContext.getOrDefault(COUNT_KEY, 0);
                    count++;
                    EventLoopContext.put(COUNT_KEY, count);
                });
            }

            for (int i = 0; i < loopNum; i++) {
                pool.next(i).execute(() -> System.out.println(EventLoopContext.get(COUNT_KEY).toString()));
            }

            Thread.sleep(5_000);
        } finally {
            pool.shutdown();
        }
    }
}
