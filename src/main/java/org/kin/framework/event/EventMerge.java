package org.kin.framework.event;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 事件合并参数,
 * 注解在{@link EventFunction}和{@link EventHandler}上, 标识需要提供事件合并
 *
 * @author huangjianqin
 * @date 2021/3/13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface EventMerge {
    /** 事件合并类型 */
    MergeType type() default MergeType.WINDOW;

    /** 窗口时间, 单位ms */
    long window();

    /** 时间单位 */
    TimeUnit unit() default TimeUnit.MILLISECONDS;
}
