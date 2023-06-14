package org.kin.framework.collection;

import org.kin.framework.common.AbstractConstant;
import org.kin.framework.common.ConstantPool;

/**
 * 通过{@link AttributeKey}可以直接访问{@link AttributeMap}中的{@link DefaultAttribute}
 * 一般不会存在重复名称的{@link AttributeKey}实例
 *
 * Forked from <a href="https://github.com/netty/netty">Netty</a>.
 * @author huangjianqin
 * @date 2023/6/14
 */
public class AttributeKey<T> extends AbstractConstant<AttributeKey<T>> {
    /** {@link AttributeKey}实例所属常量池 */
    private static final ConstantPool<AttributeKey<Object>> POOL = new ConstantPool<AttributeKey<Object>>() {
        @Override
        protected AttributeKey<Object> newConstant(int id, String name) {
            return new AttributeKey<>(id, name);
        }
    };

    /**
     * 返回名为{@code name}的{@link AttributeKey}单例
     */
    @SuppressWarnings("unchecked")
    public static <T> AttributeKey<T> valueOf(String name) {
        return (AttributeKey<T>) POOL.valueOf(name);
    }

    /**
     * 返回是否存在名为{@code name}的{@link AttributeKey}单例
     */
    public static boolean exists(String name) {
        return POOL.exists(name);
    }

    /**
     * 创建名为{@code name}{@link AttributeKey}实例, 如果已存在, 则抛异常
     */
    @SuppressWarnings("unchecked")
    public static <T> AttributeKey<T> create(String name) {
        return (AttributeKey<T>) POOL.create(name);
    }

    /**
     * 返回名为'[class name]#[custom name]'的{@link AttributeKey}单例
     */
    @SuppressWarnings("unchecked")
    public static <T> AttributeKey<T> valueOf(Class<?> firstNameComponent, String secondNameComponent) {
        return (AttributeKey<T>) POOL.valueOf(firstNameComponent, secondNameComponent);
    }

    private AttributeKey(int id, String name) {
        super(id, name);
    }
}