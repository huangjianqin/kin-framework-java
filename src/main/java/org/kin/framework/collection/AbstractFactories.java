package org.kin.framework.collection;

import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.CollectionUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * 工厂管理抽象
 *
 * @author huangjianqin
 * @date 2021/12/25
 */
public abstract class AbstractFactories<F extends Factory> {
    /** key -> 工厂创建的实例类型, value -> 工厂实例 */
    private volatile Map<Class<?>, F> factories = Collections.emptyMap();
    /** 工厂接口 */
    private final Class<F> factoryType;

    @SuppressWarnings("unchecked")
    protected AbstractFactories() {
        //获取父类泛型类型
        List<Class<?>> types = ClassUtils.getSuperClassGenericRawTypes(getClass());
        factoryType = (Class<F>) types.get(0);
    }

    /**
     * 根据类型获取工厂
     */
    public final F getFactory(Class<?> type) {
        if (Objects.isNull(type)) {
            throw new IllegalArgumentException("type is null!");
        }

        return factories.get(type);
    }

    /**
     * 暴露给user, 注册工厂
     */
    public synchronized final void register(F factory) {
        List<Type> actualTypes = ClassUtils.getSuperInterfacesGenericActualTypes(factoryType, factory.getClass());
        register((Class<?>) actualTypes.get(0), factory);
    }

    /**
     * 暴露给user, 注册工厂
     */
    public synchronized void register(Class<?> type, F factory) {
        if (Objects.isNull(type) || Objects.isNull(factory)) {
            throw new IllegalArgumentException("type or factory is null!");
        }
        register(Collections.singletonMap(type, factory));
    }

    /**
     * 暴露给user, 注册工厂
     */
    public synchronized final void register(Map<Class<?>, F> newFactories) {
        if (CollectionUtils.isEmpty(newFactories)) {
            return;
        }
        Map<Class<?>, F> factories = new HashMap<>(this.factories);
        factories.putAll(newFactories);
        this.factories = factories;
    }
}
