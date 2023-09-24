package org.kin.framework.collection;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author huangjianqin
 * @date 2023/9/23
 */
public interface ConfigurationProperties {
    /**
     * 批量添加properties
     *
     * @param properties properties
     */
    void putAll(Map<String, ?> properties);

    /**
     * 添加property
     *
     * @param key property key
     * @param obj property value
     * @return this
     */
    @Nullable
    <T> T put(String key, Object obj);

    /**
     * 是否存在property key
     *
     * @param key property key
     * @return true表示存在property key
     */
    boolean contains(String key);

    /**
     * 返回property value
     *
     * @param key property key
     * @return property value
     */
    @Nullable
    <T> T get(String key);

    /**
     * 返回property value, 如果不存在则取{@code defaultValue}
     *
     * @param key          property key
     * @param defaultValue 默认property value
     * @return property value
     */
    <T> T get(String key, T defaultValue);

    /**
     * 返回property boolean value
     *
     * @param key property key
     * @return property boolean value
     */
    default boolean getBool(String key) {
        return getBool(key, false);
    }

    /**
     * 返回property boolean value, 如果不存在则取{@code defaultValue}
     *
     * @param key          property key
     * @param defaultValue 默认property boolean value
     * @return property boolean value
     */
    boolean getBool(String key, boolean defaultValue);

    /**
     * 返回property byte value
     *
     * @param key property key
     * @return property byte value
     */
    default byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    /**
     * 返回property byte value, 如果不存在则取{@code defaultValue}
     *
     * @param key          property key
     * @param defaultValue 默认property byte value
     * @return property byte value
     */
    byte getByte(String key, byte defaultValue);

    /**
     * 返回property short value
     *
     * @param key property key
     * @return property short value
     */
    default short getShort(String key) {
        return getShort(key, (short) 0);
    }

    /**
     * 返回property short value, 如果不存在则取{@code defaultValue}
     *
     * @param key          property key
     * @param defaultValue 默认property short value
     * @return property short value
     */
    short getShort(String key, short defaultValue);

    /**
     * 返回property int value
     *
     * @param key property key
     * @return property int value
     */
    default int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * 返回property int value, 如果不存在则取{@code defaultValue}
     *
     * @param key          property key
     * @param defaultValue 默认property int value
     * @return property int value
     */
    int getInt(String key, int defaultValue);

    /**
     * 返回property long value
     *
     * @param key property key
     * @return property long value
     */
    default long getLong(String key) {
        return getLong(key, 0L);
    }

    /**
     * 返回property long value, 如果不存在则取{@code defaultValue}
     *
     * @param key          property key
     * @param defaultValue 默认property long value
     * @return property long value
     */
    long getLong(String key, long defaultValue);

    /**
     * 返回property float value
     *
     * @param key property key
     * @return property float value
     */
    default float getFloat(String key) {
        return getFloat(key, 0F);
    }

    /**
     * 返回property float value, 如果不存在则取{@code defaultValue}
     *
     * @param key          property key
     * @param defaultValue 默认property float value
     * @return property float value
     */
    float getFloat(String key, float defaultValue);

    /**
     * 返回property double value
     *
     * @param key property key
     * @return property double value
     */
    default double getDouble(String key) {
        return getDouble(key, 0D);
    }

    /**
     * 返回property double value, 如果不存在则取{@code defaultValue}
     *
     * @param key          property key
     * @param defaultValue 默认property double value
     * @return property double value
     */
    double getDouble(String key, double defaultValue);

    /**
     * 返回property value, 并使用{@code func}进行值转换
     *
     * @param key  property key
     * @param func property convert function
     * @return property value
     */
    @Nullable
    <T> T get(String key, Function<Object, T> func);

    /**
     * 返回property value, 并使用{@code func}进行值转换, 如果不存在则取{@code defaultValue}
     *
     * @param key          property key
     * @param func         property convert function
     * @param defaultValue 默认property value
     * @return property value
     */
    <T> T get(String key, Function<Object, T> func, T defaultValue);

    /**
     * 移除property
     *
     * @param key property key
     * @return property value if exists
     */
    @Nullable
    <T> T remove(String key);

    /**
     * 返回所有property
     *
     * @return 所有property
     */
    Map<String, Object> toMap();

    /**
     * 返回所有property
     *
     * @return 所有property
     */
    default Map<String, String> toProperties() {
        return toMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
    }

    /**
     * 移除所有property
     */
    void clear();
}
