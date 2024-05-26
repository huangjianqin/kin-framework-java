package org.kin.framework.utils;

/**
 * MD5工具类
 * 非线程安全
 *
 * @author huangjianqin
 * @date 2022/7/2
 */
public class MD5 extends AbstractMessageDigest {
    /** 默认MD5 */
    private static final MD5 COMMON = new MD5();
    /** thread local MD5 */
    private static final ThreadLocal<MD5> THREAD_LOCAL_MD5 = ThreadLocal.withInitial(MD5::new);

    /**
     * 返回默认MD5
     */
    public static MD5 common() {
        return COMMON;
    }

    /**
     * 返回thread local MD5
     */
    public static MD5 current() {
        return THREAD_LOCAL_MD5.get();
    }

    public MD5() {
        super("MD5");
    }
}
