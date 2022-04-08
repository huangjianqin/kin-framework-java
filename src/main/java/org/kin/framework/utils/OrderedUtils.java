package org.kin.framework.utils;

import org.kin.framework.common.Ordered;

import java.util.Comparator;
import java.util.List;

/**
 * {@link org.kin.framework.common.Ordered}的工具类
 *
 * @author huangjianqin
 * @date 2021/3/15
 */
@SuppressWarnings("rawtypes")
public final class OrderedUtils {
    private OrderedUtils() {
    }

    /** order Comparator */
    private static final Comparator DEFAULT = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            return Integer.compare(getOrder(o2), getOrder(o1));
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    };

    public static int getOrder(Object o) {
        return getOrder(o, Ordered.LOWEST_PRECEDENCE);
    }

    /**
     * 根据规则获取order value, 则返回, 否则返回default
     *
     * @param defaultOrder 默认order value
     * @return 该实例的order value
     */
    public static int getOrder(Object o, int defaultOrder) {
        if (o instanceof Ordered) {
            return ((Ordered) o).order();
        }

        return defaultOrder;
    }

    /**
     * item实现了{@link org.kin.framework.common.Ordered}接口的集合类排序
     */
    @SuppressWarnings("unchecked")
    public static void sort(List<?> list) {
        list.sort(DEFAULT);
    }
}
