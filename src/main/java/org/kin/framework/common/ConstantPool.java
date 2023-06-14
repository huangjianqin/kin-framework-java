package org.kin.framework.common;

import org.kin.framework.collection.CopyOnWriteMap;
import org.kin.framework.utils.StringUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 常量池
 *
 * @author huangjianqin
 * @date 2023/6/14
 */
@ThreadSafe
public abstract class ConstantPool<T extends Constant<T>> {
    /** 常量池 */
    private final Map<String, T> constants = new CopyOnWriteMap<>();
    /** 常量唯一id生成 */
    private final AtomicInteger nextId = new AtomicInteger(1);

    /**
     * 检查常量名是否为空
     *
     * @param name 常量名
     * @return 常量名
     */
    private static String requireNotBlank(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name is blank");
        }

        return name;
    }

    /**
     * 生成常量唯一id
     *
     * @return 常量唯一id
     */
    private final int nextId() {
        return nextId.getAndIncrement();
    }

    /**
     * 获取名为'[class name]#[custom name]'的{@link Constant}实例, 如果没有, 则创建一个
     * @param firstNameComponent    class name
     * @param secondNameComponent   custom name
     * @return  {@link Constant}实例
     */
    public T valueOf(Class<?> firstNameComponent, String secondNameComponent) {
        return valueOf(
                Objects.requireNonNull(firstNameComponent, "firstNameComponent").getName() +
                        '#' +
                        Objects.requireNonNull(secondNameComponent, "secondNameComponent"));
    }

    /**
     * 获取名为{@code name}的{@link Constant}实例, 如果没有, 则创建一个
     *
     * @param name 常量名
     * @return {@link Constant}实例
     */
    public T valueOf(String name) {
        return getOrCreate(requireNotBlank(name));
    }

    /**
     * 获取名为{@code name}的{@link Constant}实例, 如果没有, 则创建一个
     *
     * @param name 常量名
     * @return {@link Constant}实例
     */
    private T getOrCreate(String name) {
        return constants.computeIfAbsent(name, k -> newConstant(nextId(), name));
    }

    /**
     * 是否已存在名为{@code name}的{@link Constant}实例
     *
     * @param name 常量名
     * @return true表示已存在称为{@code name}的{@link Constant}实例
     */
    public boolean exists(String name) {
        return constants.containsKey(requireNotBlank(name));
    }

    /**
     * 创建一个新的名为{@code name}的{@link Constant}实例, 如果已存在, 则抛异常
     *
     * @param name 常量名
     * @return {@link Constant}实例
     */
    public T create(String name) {
        return createOrThrow(requireNotBlank(name));
    }

    /**
     * 创建一个新的名为{@code name}的{@link Constant}实例, 如果已存在, 则抛异常
     *
     * @param name 常量名
     * @return {@link Constant}实例
     */
    private T createOrThrow(String name) {
        T constant = constants.get(name);
        if (constant == null) {
            final T tempConstant = newConstant(nextId(), name);
            constant = constants.putIfAbsent(name, tempConstant);
            if (constant == null) {
                return tempConstant;
            }
        }

        throw new IllegalArgumentException(String.format("constant '%s' is already in use", name));
    }

    /**
     * 创建一个名为{@code name}且id={@code id}的{@link Constant}实例
     *
     * @param id   常量唯一id
     * @param name 常量名
     * @return {@link Constant}实例
     */
    protected abstract T newConstant(int id, String name);
}
