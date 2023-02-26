package org.kin.framework.proxy;

import java.util.Objects;

/**
 * javassist与byte buddy性能接近, 但javassist make class会大量消耗metaspace,
 * 尤其是CompressedClassSpaceSize, 当使用javassist创建大量class时, 需调大-XX:CompressedClassSpaceSize(最大3g)
 * 还有javassist ClassPool.get()方法时, 加载ClassPath已有的类, 会读取对应的class文件, 如果在生产环境存在, class文件加密的话, 会出现异常
 *
 * @author huangjianqin
 * @date 2020/12/22
 */
public class Proxys {
    /** 是否支持ByteBuddy字节码增强 */
    private static final boolean BYTE_BUDDY_ENHANCE;
    /** 是否支持Javassist字节码增强 */
    private static final boolean JAVASSIST_ENHANCE;

    static {
        Class<?> byteBuddyClass = null;
        try {
            byteBuddyClass = Class.forName("net.bytebuddy.ByteBuddy");
        } catch (Exception e) {
            //ignore
        }

        BYTE_BUDDY_ENHANCE = Objects.nonNull(byteBuddyClass);

        Class<?> javassistClass = null;
        try {
            javassistClass = Class.forName("javassist.CtClass");
        } catch (Exception e) {
            //ignore
        }
        JAVASSIST_ENHANCE = Objects.nonNull(javassistClass);
    }

    private Proxys() {
    }

    /**
     * @return javassist 工厂类
     */
    public static ProxyFactory javassist() {
        return JavassistFactory.INSTANCE;
    }

    /**
     * @return byteBuddy 工厂类
     */
    public static ProxyFactory byteBuddy() {
        return ByteBuddyFactory.INSTANCE;
    }

    /**
     * @return jdk 工厂类
     */
    public static ProxyFactory reflection() {
        return ReflectionProxyFactory.INSTANCE;
    }

    /**
     * 根据环境自适应获取{@link ProxyFactory}
     *
     * @return {@link ProxyFactory}实例
     */
    public static ProxyFactory adaptive() {
        if (isByteBuddyEnhance()) {
            return byteBuddy();
        }

        if (isJavassistEnhance()) {
            return javassist();
        }

        return reflection();
    }

    /**
     * 当前环境是否支持ByteBuddy字节码增强
     */
    public static boolean isByteBuddyEnhance() {
        return BYTE_BUDDY_ENHANCE;
    }

    /**
     * 当前环境是否支持Javassist字节码增强
     */
    public static boolean isJavassistEnhance() {
        return BYTE_BUDDY_ENHANCE;
    }
}
