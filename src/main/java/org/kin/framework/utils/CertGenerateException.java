package org.kin.framework.utils;

/**
 * 生成证书异常
 *
 * @author huangjianqin
 * @date 2024/5/25
 */
public class CertGenerateException extends RuntimeException {
    private static final long serialVersionUID = -2051536899217483326L;

    public CertGenerateException() {
    }

    public CertGenerateException(String message) {
        super(message);
    }

    public CertGenerateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CertGenerateException(Throwable cause) {
        super(cause);
    }

    public CertGenerateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
