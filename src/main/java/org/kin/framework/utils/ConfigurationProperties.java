package org.kin.framework.utils;

import java.lang.annotation.*;

/**
 * 跟spring boot的@ConfigurationProperties一样
 * 1. 注解在class上, 定义property前缀
 * 2. 注解在public方法上, 参数必须为string, 调用这个方法
 * <p>
 * 逻辑上先遍历字段后遍历方法
 * !注意最后一个字符不要是'.', 不然会报错
 *
 * @author huangjianqin
 * @date 2022/11/28
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {
    /** property key前缀 */
    String value() default "";
}
