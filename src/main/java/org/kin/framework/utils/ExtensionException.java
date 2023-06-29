package org.kin.framework.utils;

/**
 * @author huangjianqin
 * @date 2023/6/29
 */
public class ExtensionException extends RuntimeException {
    private static final long serialVersionUID = -8463098156605076625L;

    public ExtensionException() {
    }

    public ExtensionException(String message) {
        super(message);
    }

    public ExtensionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtensionException(Throwable cause) {
        super(cause);
    }

    public ExtensionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
