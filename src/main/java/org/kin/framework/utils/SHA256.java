package org.kin.framework.utils;

/**
 * SHA256属于不可逆的加密算法, 即无法对密文进行解密
 * 与MD5相比, SHA256散列值长度更长, 为256位
 *
 * @author huangjianqin
 * @date 2024/5/21
 */
public class SHA256 extends AbstractMessageDigest {
    /** 默认SHA256 */
    private static final SHA256 COMMON = new SHA256();
    /** thread local SHA256 */
    private static final ThreadLocal<SHA256> THREAD_LOCAL_SHA256 = ThreadLocal.withInitial(SHA256::new);

    /**
     * 返回默认SHA256
     */
    public static SHA256 common() {
        return COMMON;
    }

    /**
     * 返回thread local SHA256
     */
    public static SHA256 current() {
        return THREAD_LOCAL_SHA256.get();
    }

    public SHA256() {
        super("SHA-256");
    }
}
