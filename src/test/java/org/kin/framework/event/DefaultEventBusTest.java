package org.kin.framework.event;

import org.kin.framework.common.Ordered;

import java.util.List;

/**
 * Created by huangjianqin on 2019/3/30.
 */
@EventListener
public class DefaultEventBusTest {
    public static void main(String[] args) throws InterruptedException {
        DefaultEventBus eventBus = new DefaultEventBus();
        eventBus.register(new DefaultEventBusTest());
        eventBus.register(new FirstEventHandler());

        eventBus.post(new FirstEvent());
        eventBus.post(new SecondEvent());

        System.out.println(System.currentTimeMillis() + " >>> post ThirdEvent to event bus");
        for (int i = 0; i < 8; i++) {
            eventBus.post(new ThirdEvent());
            Thread.sleep(200);
        }
        System.out.println(System.currentTimeMillis() + " >>> finish post ThirdEvent to event bus");

        Thread.sleep(5_000);
        System.out.println("event bus prepare to shutdown");
        eventBus.shutdown();
    }

    @EventFunction(order = Ordered.HIGHEST_PRECEDENCE)
    public void handleFirstEvent1(FirstEvent event) {
        System.out.println("1 >>> handle " + event.toString());
    }

    @EventFunction
    public void handleFirstEvent2(FirstEvent event) {
        System.out.println("2 >>> handle " + event.toString());
    }

    @EventFunction
    public void handleSecondEvent(SecondEvent event) {
        System.out.println("handle " + event.toString());
    }

    @EventMerge(window = 1000)
    @EventFunction
    public void handleThirdEvent1(List<ThirdEvent> events) {
        System.out.println(System.currentTimeMillis() + ": handleThirdEvent1 >>> handle " + events);
    }

    @EventMerge(type = MergeType.DEBOUNCE, window = 1000, maxSize = 3)
    @EventFunction
    public void handleThirdEvent2(List<ThirdEvent> events) {
        System.out.println(System.currentTimeMillis() + ": handleThirdEvent2 >>> handle " + events);
    }
}
