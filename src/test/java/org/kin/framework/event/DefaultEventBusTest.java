package org.kin.framework.event;

import java.util.List;

/**
 * Created by huangjianqin on 2019/3/30.
 */
public class DefaultEventBusTest {
    @EventFunction
    public void handleFirstEvent(FirstEvent event) {
        System.out.println("handle " + event.toString());
    }

    @EventFunction
    public void handleSecondEvent(SecondEvent event) {
        System.out.println("handle " + event.toString());
    }

//    @EventFunction
//    public void handleThirdEvent(List<ThirdEvent> events) {
//        System.out.println("handle " + events);
//    }

    public static void main(String[] args) throws InterruptedException {
        DefaultEventBus eventBus = new DefaultEventBus();
        eventBus.register(new DefaultEventBusTest());

        eventBus.post(new FirstEvent());
        eventBus.post(new SecondEvent());

//        for (int i = 0; i < 8; i++) {
//            eventBus.post(new ThirdEvent());
//            Thread.sleep(200);
//        }

        Thread.sleep(5_000);
        eventBus.shutdown();
    }
}
