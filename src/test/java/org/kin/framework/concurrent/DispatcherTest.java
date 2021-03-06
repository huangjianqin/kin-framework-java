package org.kin.framework.concurrent;

import org.kin.framework.collection.ConcurrentHashSet;

import java.util.Set;

/**
 * @author huangjianqin
 * @date 2020-04-16
 */
public class DispatcherTest {
    private static Set<String> names = new ConcurrentHashSet<>();
    private static int counter;

    public static void main(String[] args) throws InterruptedException {
        ExecutionContext executionContext = ExecutionContext.cache("dispatcher-test");

        Dispatcher<Integer, IntMessage> dispatcher = new EventBasedDispatcher<>(5);
        int key = 1;
        dispatcher.register(key, new TestReceiver(), false);

        int num = 3;
        for (int i = 0; i < num; i++) {
            executionContext.execute(new TestRunnable(dispatcher));
        }

        Thread.sleep(10000);

        System.out.println(names);
        System.out.println(counter);

        dispatcher.close();
        executionContext.shutdown();
    }

    static class TestReceiver extends Receiver<IntMessage> {
        @Override
        public void receive(IntMessage message) {
            //保存线程名
            names.add(Thread.currentThread().getName());
            //累加
            counter++;
        }

        @Override
        protected void onStart() {
            super.onStart();
            System.out.println("receiver start");
        }

        @Override
        protected void onStop() {
            super.onStop();
            System.out.println("receiver stop");
        }
    }

    static class IntMessage extends InBox.InBoxMessage {
        private int i;

        public IntMessage(int i) {
            this.i = i;
        }
    }


    static class TestRunnable implements Runnable {
        private Dispatcher<Integer, IntMessage> eventBaseDispatcher;

        public TestRunnable(Dispatcher<Integer, IntMessage> eventBaseDispatcher) {
            this.eventBaseDispatcher = eventBaseDispatcher;
        }

        @Override
        public void run() {
            int key = 1;
            //post message
            for (int i = 0; i < 10000; i++) {
                eventBaseDispatcher.postMessage(key, new IntMessage(i));
            }
        }
    }
}
