package org.kin.framework.concurrent;

import org.kin.framework.Closeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Objects;

/**
 * '邮箱', 即消息队列, 存储待处理的消息
 * @author huangjianqin
 * @date 2020-04-15
 */
class InBox<MSG> implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(InBox.class);

    /** 绑定的{@link Receiver}实例 */
    private final Receiver<MSG> receiver;
    /** 是否允许并发 */
    private final boolean enableConcurrent;
    /** 消息队列 */
    private final LinkedList<InBoxMessage> mail = new LinkedList<>();
    /** 正在处理消息的线程数 */
    private int activeThreads = 0;
    /** '邮箱'是否关了 */
    private boolean stopped;

    InBox(Receiver<MSG> receiver, boolean enableConcurrent) {
        this.receiver = receiver;
        this.enableConcurrent = enableConcurrent;
        mail.add(StartSignal.INSTANCE);
    }

    /**
     * 消息入队
     */
    public void post(InBoxMessage message) {
        synchronized (this) {
            if (stopped) {
                log.warn(String.format("drop %s because %s is stopped", message, receiver));
                return;
            }
            mail.add(message);
        }
    }

    /**
     * 处理消息
     */
    @SuppressWarnings("unchecked")
    public void process() {
        InBoxMessage message;
        synchronized (this) {
            if (!enableConcurrent && activeThreads > 0) {
                return;
            }
            message = mail.poll();
            if (Objects.nonNull(message)) {
                activeThreads++;
            } else {
                return;
            }
        }

        while (true) {
            try {
                if (message instanceof InBox.StartSignal) {
                    receiver.onStart();
                } else if (message instanceof InBox.ShutdownSignal) {
                    receiver.onStop();
                } else if (message instanceof InBox.ReceiverMessage) {
                    ReceiverMessage<MSG> receiverMessage = (ReceiverMessage<MSG>) message;
                    receiver.receive(receiverMessage.getMessage());
                } else {
                    log.error("unknown InBoxMessage >>>> {}", message);
                }
            } catch (Exception e) {
                log.error("", e);
            }

            synchronized (this) {
                if (!enableConcurrent && activeThreads != 1) {
                    activeThreads--;
                    return;
                }
                message = mail.poll();
                if (Objects.isNull(message)) {
                    activeThreads--;
                    return;
                }
            }
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if (!stopped) {
                stopped = true;
                mail.add(ShutdownSignal.INSTANCE);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InBox)) {
            return false;
        }
        InBox<?> inBox = (InBox<?>) o;
        return Objects.equals(receiver, inBox.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiver);
    }

    //-------------------------------------------------------------------------------------------------------

    /**
     * {@link InBox}消息
     */
    static class InBoxMessage {
    }

    /**
     * 封装{@link Receiver}处理的消息的InBoxMessage
     */
    final static class ReceiverMessage<M> extends InBoxMessage {
        private final M message;

        ReceiverMessage(M message) {
            this.message = message;
        }

        public M getMessage() {
            return message;
        }
    }

    /**
     * start signal
     */
    final static class StartSignal extends InBoxMessage {
        static final InBoxMessage INSTANCE = new StartSignal();
    }

    /**
     * shutdown signal
     */
    final static class ShutdownSignal extends InBoxMessage {
        static final InBoxMessage INSTANCE = new ShutdownSignal();
    }
}
