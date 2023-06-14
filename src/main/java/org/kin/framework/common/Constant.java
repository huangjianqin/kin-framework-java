package org.kin.framework.common;

/**
 * 常量定义, 单例
 * 可以通过==对比
 * 作为map key时, 还可以通过{@link #id()}进行快速equal操作
 * 由{@link ConstantPool}管理
 *
 * 参考netty
 * @author huangjianqin
 * @date 2023/6/14
 */
public interface Constant <T extends Constant<T>> extends Comparable<T> {
    /**
     * 返回常量id
     * @return 常量id
     */
    int id();

    /**
     * 返回常量名
     * @return 常量名
     */
    String name();
}
