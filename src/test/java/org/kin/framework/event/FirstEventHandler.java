package org.kin.framework.event;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * @author huangjianqin
 * @date 2020-01-12
 */
public class FirstEventHandler implements EventHandler<FirstEvent> {
    @Override
    public void handle(EventBus eventBus, FirstEvent event) {
        System.out.println(Thread.currentThread().getName() + " >>> handle " + event.toString());
    }

    @Override
    public Executor executor() {
        return ForkJoinPool.commonPool();
    }
}
