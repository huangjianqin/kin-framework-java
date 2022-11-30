package org.kin.framework.utils;

import java.lang.annotation.*;

/**
 * spring boot的@ConfigurationProperties简化版
 * 注解在class上, 定义property前缀
 * <p>
 * 注意
 * 1. 最后一个字符不要是'.', 不然会匹配异常
 * 2. 嵌套properties bean使用该注解时, 假如parent bean prefix是a.b, 该bean变量名是c, 同时prefix是d,
 * 那么最后该bean的前缀是a.b.c.d
 * 3. 子类会覆盖父类的注解定义
 *
 * @author huangjianqin
 * @date 2022/11/28
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {
    /** property key前缀 */
    String value() default "";
}
