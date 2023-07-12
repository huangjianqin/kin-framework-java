package org.kin.framework.concurrent;

import org.kin.framework.utils.SysUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 底层消息处理实现是每个{@link Receiver}绑定一条线程, 该线程由一个线程池管理(该线程池可以固定线程数, 也可以无限线程数)
 * 消息有序处理, 但不保证在同一线程下执行, 不要使用{@link ThreadLocal}
 * 可以blocking, 但要控制好parallelism, 保证有足够的线程数
 *
 * @author huangjianqin
 * @date 2020-04-26
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class OrderedDispatcher<KEY, MSG> extends AbstractDispatcher<KEY, MSG> {
    /** 底层event loop group */
    private final CachedOrderedEventLoopGroup eventLoopGroup;
    /** 注册的{@link Receiver} */
    private final Map<KEY, EventLoopReceiver<MSG>> receiverMap = new ConcurrentHashMap<>();

    public OrderedDispatcher(int parallelism) {
        this(parallelism, "orderedDispatcher");
    }

    public OrderedDispatcher(int parallelism, String workerNamePrefix) {
        this(ExecutionContext.elastic(parallelism, Math.min(parallelism, SysUtils.CPU_NUM * 3),
                workerNamePrefix, SysUtils.CPU_NUM / 2 + 1));
    }

    public OrderedDispatcher(ExecutionContext executionContext) {
        super(executionContext);
        eventLoopGroup = new CachedOrderedEventLoopGroup(super.executionContext, OrderedEventLoop::new);
    }

    @Override
    public void register(KEY key, Receiver<MSG> receiver, boolean enableConcurrent) {
        if (isStopped()) {
            throw new IllegalStateException("dispatcher is closed");
        }

        if (Objects.isNull(key)) {
            throw new IllegalArgumentException("key is null");
        }

        if (Objects.isNull(receiver)) {
            throw new IllegalArgumentException("receiver is null");
        }

        //保证receiver 先进行start, 后stop
        synchronized (this) {
            if (Objects.nonNull(receiverMap.putIfAbsent(key, new EventLoopReceiver<>(receiver)))) {
                throw new IllegalArgumentException(String.format("receiver with key `%s` has registered", key));
            }

            receiverMap.get(key).onStart();
        }
    }

    @Override
    public void unregister(KEY key) {
        if (isStopped()) {
            return;
        }

        unregister0(key);
    }

    private void unregister0(KEY key){
        if (Objects.isNull(key)) {
            throw new IllegalArgumentException("key is null");
        }

        //保证receiver 先进行start, 后stop
        synchronized (this) {
            EventLoopReceiver<MSG> receiver = receiverMap.remove(key);
            if (Objects.nonNull(receiver)) {
                receiver.onStop();
            }
        }
    }

    @Override
    public boolean isRegistered(KEY key) {
        if (isStopped()) {
            return false;
        }

        return receiverMap.containsKey(key);
    }

    @Override
    public void postMessage(KEY key, MSG message) {
        if (isStopped()) {
            throw new IllegalStateException("dispatcher is closed");
        }

        if (Objects.isNull(key)) {
            throw new IllegalArgumentException("key is null");
        }

        if (Objects.isNull(message)) {
            throw new IllegalArgumentException("message is null");
        }

        EventLoopReceiver<MSG> receiver = receiverMap.get(key);
        if (Objects.nonNull(receiver)) {
            receiver.receive(message);
        }
    }

    @Override
    public void post2All(MSG message) {
        if (isStopped()) {
            throw new IllegalStateException("dispatcher is closed");
        }

        if (Objects.isNull(message)) {
            throw new IllegalArgumentException("message is null");
        }

        for (KEY key : receiverMap.keySet()) {
            postMessage(key, message);
        }
    }

    @Override
    protected void doClose() {
        receiverMap.keySet().forEach(this::unregister0);

        //help gc
        receiverMap.clear();
    }

    //-----------------------------------------------------------------------------------------------------

    /**
     * 线程安全{@link Receiver}实现
     */
    private class EventLoopReceiver<M> extends Receiver<M> {
        /** event loop */
        private final EventLoop loop;
        /** 委托的{@link Receiver} */
        private final Receiver<M> delegate;

        private EventLoopReceiver(Receiver<M> receiver) {
            this.loop = eventLoopGroup.next();
            this.delegate = receiver;
        }

        @Override
        public void receive(M message) {
            loop.receive(pal -> delegate.receive(message));
        }

        @Override
        protected void onStart() {
            loop.receive(pal -> delegate.onStart());
        }

        @Override
        protected void onStop() {
            loop.receive(pal -> {
                delegate.onStop();
                loop.shutdown();
            });
        }
    }
}
