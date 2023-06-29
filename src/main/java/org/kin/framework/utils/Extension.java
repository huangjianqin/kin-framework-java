package org.kin.framework.utils;

import java.lang.annotation.*;

/**
 * SPI机制信息扩展, 提供接口给extension class定义一些额外信息
 *
 * @author huangjianqin
 * @date 2021/11/21
 * @see SPI
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Extension {
    /**
     * extension class别名, 默认使用{@link Class#getName()}, {@link Class#getSimpleName()}和 {@link Class#getCanonicalName()}
     * 用于匹配extension实例
     *
     * @see ExtensionLoader#getExtension(Class, String)
     * @see ExtensionLoader#getExtension(Class, String, Object...)
     */
    String value() default "";

    /**
     * extension class编码，默认不需要，当扩展类需要编码的时候需要
     *
     * @return extension class编码
     * @see SPI#coded()
     */
    short code() default -1;

    /**
     * 优先级排序，默认不需要, 值越大优先级越高
     *
     * @return 排序
     */
    int order() default 0;
}
