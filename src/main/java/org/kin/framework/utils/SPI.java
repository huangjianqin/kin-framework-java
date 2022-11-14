package org.kin.framework.utils;

import java.lang.annotation.*;

/**
 * 标识接口支持SPI扩展机制
 *
 * @author huangjianqin
 * @date 2020/9/27
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SPI {
    /**
     * 定义默认extension name, 对应实现类的class name | simple class name | 前缀 + extension class simple name | {@link Extension#value()}
     *
     * @see ExtensionLoader#getExtensionOrDefault(Class, int)
     * @see ExtensionLoader#getExtensionOrDefault(Class, String)
     * @see ExtensionLoader#getExtensionOrDefault(Class, int, Object...)
     * @see ExtensionLoader#getExtensionOrDefault(Class, String, Object...)
     */
    String value() default "";

    /**
     * 定义extension class在kin.factories中定义的key, 默认class name
     */
    String alias() default "";

    /**
     * extension class是否需要编码，默认不需要, 相当于定义另外一个key, 可以类型是int, code -> extension instance
     *
     * @return 是否需要编码
     */
    boolean coded() default false;

    /**
     * extension class是否使用单例，默认使用
     *
     * @return 是否使用单例
     */
    boolean singleton() default true;
}