package org.kin.framework.event;

import java.lang.annotation.*;

/**
 * 标识声明方法带有{@link EventFunction}注解的实例
 *
 * @author huangjianqin
 * @date 2022/11/26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface EventListener {
}
