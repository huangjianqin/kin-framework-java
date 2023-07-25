package org.kin.framework.utils;

/**
 * 不合法格式异常
 *
 * @author huangjianqin
 * @date 2023/7/25
 */
public class IllegalFormatException extends RuntimeException {
    private static final long serialVersionUID = -5267419476951109289L;

    public IllegalFormatException() {
    }

    public IllegalFormatException(String message) {
        super(message);
    }

    public IllegalFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalFormatException(Throwable cause) {
        super(cause);
    }

    public IllegalFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
