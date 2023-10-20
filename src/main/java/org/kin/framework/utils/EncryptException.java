package org.kin.framework.utils;

/**
 * 通用加密异常
 * @author huangjianqin
 * @date 2023/10/18
 */
public class EncryptException extends RuntimeException{
    private static final long serialVersionUID = 7581128857879647113L;

    public EncryptException() {
    }

    public EncryptException(String message) {
        super(message);
    }

    public EncryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncryptException(Throwable cause) {
        super(cause);
    }

    public EncryptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
