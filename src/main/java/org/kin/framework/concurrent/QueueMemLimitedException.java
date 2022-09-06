package org.kin.framework.concurrent;

/**
 * @author huangjianqin
 * @date 2022/9/6
 */
public final class QueueMemLimitedException extends RuntimeException {
    private static final long serialVersionUID = 1479430719457575232L;

    public QueueMemLimitedException() {
    }

    public QueueMemLimitedException(final String message) {
        super(message);
    }

    public QueueMemLimitedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public QueueMemLimitedException(final Throwable cause) {
        super(cause);
    }
}
