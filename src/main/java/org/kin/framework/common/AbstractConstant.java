package org.kin.framework.common;

import org.kin.framework.utils.Maths;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author huangjianqin
 * @date 2023/6/14
 */
public abstract class AbstractConstant <T extends AbstractConstant<T>> implements Constant<T> {
    /** 常量唯一id生成器 */
    private static final AtomicLong ORDER_GENERATOR = new AtomicLong();
    /** 常量唯一id */
    private final int id;
    /** 常量名 */
    private final String name;
    /** 优先级 */
    private final long order;

    protected AbstractConstant(int id, String name) {
        this.id = id;
        this.name = name;
        this.order = ORDER_GENERATOR.getAndIncrement();
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final int id() {
        return id;
    }

    @Override
    public final String toString() {
        return name();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractConstant<?> that = (AbstractConstant<?>) o;
        return id == that.id;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public final int compareTo(T o) {
        if (this == o) {
            return 0;
        }

        AbstractConstant<T> other = o;
        int returnCode;

        returnCode = hashCode() - other.hashCode();
        if (returnCode != 0) {
            return returnCode;
        }

        if (order < other.order) {
            return -1;
        }
        if (order > other.order) {
            return 1;
        }

        throw new Error("failed to compare two different constants");
    }
}
