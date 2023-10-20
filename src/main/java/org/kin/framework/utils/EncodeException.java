package org.kin.framework.utils;

/**
 * 通用解码异常
 * @author huangjianqin
 * @date 2023/10/18
 */
public class EncodeException extends RuntimeException{
    private static final long serialVersionUID = 7508217594843363023L;

    public EncodeException() {
    }

    public EncodeException(String message) {
        super(message);
    }

    public EncodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncodeException(Throwable cause) {
        super(cause);
    }

    public EncodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
