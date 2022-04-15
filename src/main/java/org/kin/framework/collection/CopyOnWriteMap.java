package org.kin.framework.collection;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author huangjianqin
 * @date 2022/4/15
 */
public class CopyOnWriteMap<K, V> implements Map<K, V> {
    /** 底层map工厂方法 */
    private final MapFactory<Map<K, V>> mapFactory;
    private transient volatile Map<K, V> delegate;

    public CopyOnWriteMap() {
        this(HashMap::new);
    }

    public CopyOnWriteMap(MapFactory<Map<K, V>> mapFactory) {
        this.mapFactory = mapFactory;
        this.delegate = mapFactory.newMap();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return delegate.get(key);
    }

    @Override
    public synchronized V put(K key, V value) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        V ret = newDelegate.put(key, value);

        this.delegate = newDelegate;
        return ret;
    }

    @Override
    public synchronized V remove(Object key) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        V ret = newDelegate.remove(key);

        this.delegate = newDelegate;
        return ret;
    }

    @Override
    public synchronized void putAll(@Nonnull Map<? extends K, ? extends V> m) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        newDelegate.putAll(m);

        this.delegate = newDelegate;
    }

    @Override
    public synchronized void clear() {
        this.delegate = mapFactory.newMap();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        newDelegate.replaceAll(function);

        this.delegate = newDelegate;
    }

    @Override
    public synchronized V putIfAbsent(K key, V value) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        V ret = newDelegate.putIfAbsent(key, value);

        this.delegate = newDelegate;
        return ret;
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        boolean ret = newDelegate.remove(key, value);

        this.delegate = newDelegate;
        return ret;
    }

    @Override
    public synchronized boolean replace(K key, V oldValue, V newValue) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        boolean ret = newDelegate.replace(key, oldValue, newValue);

        this.delegate = newDelegate;
        return ret;
    }

    @Override
    public synchronized V replace(K key, V value) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        V ret = newDelegate.replace(key, value);

        this.delegate = newDelegate;
        return ret;
    }

    @Override
    public synchronized V computeIfAbsent(K key, @Nonnull Function<? super K, ? extends V> mappingFunction) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        V ret = newDelegate.computeIfAbsent(key, mappingFunction);

        this.delegate = newDelegate;
        return ret;
    }

    @Override
    public synchronized V computeIfPresent(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        V ret = newDelegate.computeIfPresent(key, remappingFunction);

        this.delegate = newDelegate;
        return ret;
    }

    @Override
    public synchronized V compute(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        V ret = newDelegate.compute(key, remappingFunction);

        this.delegate = newDelegate;
        return ret;
    }

    @Override
    public synchronized V merge(K key, @Nonnull V value, @Nonnull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Map<K, V> newDelegate = mapFactory.newMap();
        newDelegate.putAll(delegate);
        V ret = newDelegate.merge(key, value, remappingFunction);

        this.delegate = newDelegate;
        return ret;
    }

    @Override
    public String toString() {
        return "CopyOnWriteMap" + delegate;
    }

    //getter
    public Map<K, V> getMap() {
        return delegate;
    }
}