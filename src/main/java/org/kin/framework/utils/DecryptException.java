package org.kin.framework.utils;

/**
 * 通用解密异常
 * @author huangjianqin
 * @date 2023/10/18
 */
public class DecryptException extends RuntimeException{
    private static final long serialVersionUID = 6444234667111129610L;

    public DecryptException() {
    }

    public DecryptException(String message) {
        super(message);
    }

    public DecryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecryptException(Throwable cause) {
        super(cause);
    }

    public DecryptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
