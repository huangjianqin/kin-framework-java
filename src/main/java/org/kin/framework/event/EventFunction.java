package org.kin.framework.event;

import org.kin.framework.common.Ordered;

import java.lang.annotation.*;

/**
 * 标识事件处理具体逻辑方法
 *
 * @author huangjianqin
 * @date 2019/3/1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface EventFunction {
    /** event handler顺序 */
    int order() default Ordered.LOWEST_PRECEDENCE;
}
