package org.kin.framework.utils;

/**
 * 通用编码异常
 * @author huangjianqin
 * @date 2023/10/18
 */
public class DecodeException extends RuntimeException{
    private static final long serialVersionUID = 2147701561899131146L;

    public DecodeException() {
    }

    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodeException(Throwable cause) {
        super(cause);
    }

    public DecodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
