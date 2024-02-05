package org.kin.framework.collection;

import org.kin.framework.utils.CollectionUtils;
import org.kin.framework.utils.IllegalFormatException;
import org.kin.framework.utils.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * @author huangjianqin
 * @date 2023/9/23
 */
public class MapConfigurationProperties implements ConfigurationProperties, Map<String, Object> {
    /** properties */
    private final Map<String, Object> properties;

    public MapConfigurationProperties() {
        this(Collections.emptyMap());
    }

    public MapConfigurationProperties(Map<String, ?> properties) {
        this.properties = new HashMap<>(properties);
    }

    @Override
    public void putAll(Map<? extends String, ?> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return;
        }
        this.properties.putAll(properties);
    }

    @Nullable
    @Override
    public Object put(String key, Object obj) {
        return properties.put(key, obj);
    }

    @Override
    public boolean contains(String key) {
        return properties.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T get(String key) {
        return (T) properties.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, T defaultValue) {
        return (T) properties.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean getBool(String key, boolean defaultValue) {
        Object value = get(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Boolean.class.equals(valueClass)) {
                return (boolean) value;
            } else if (String.class.equals(valueClass)) {
                String valueStr = (String) value;
                if (StringUtils.isNumeric(valueStr)) {
                    return Long.parseLong(valueStr) > 0;
                } else {
                    return Boolean.parseBoolean(value.toString().trim());
                }
            } else {
                throw new IllegalFormatException(String.format("property '%s' is not a boolean", value));
            }
        }
        return defaultValue;
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        Object value = get(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Byte.class.equals(valueClass)) {
                return (byte) value;
            } else if (String.class.equals(valueClass)) {
                return Byte.parseByte(value.toString().trim());
            } else {
                throw new IllegalFormatException(String.format("property '%s' is not a byte", value));
            }
        }
        return defaultValue;
    }

    @Override
    public short getShort(String key, short defaultValue) {
        Object value = get(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Short.class.equals(valueClass)) {
                return (short) value;
            } else if (String.class.equals(valueClass)) {
                return Short.parseShort(value.toString().trim());
            } else {
                throw new IllegalFormatException(String.format("property '%s' is not a short", value));
            }
        }
        return defaultValue;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        Object value = get(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Integer.class.equals(valueClass)) {
                return (int) value;
            } else if (String.class.equals(valueClass)) {
                return Integer.parseInt(value.toString().trim());
            } else {
                throw new IllegalFormatException(String.format("property '%s' is not a integer", value));
            }
        }
        return defaultValue;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        Object value = get(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Long.class.equals(valueClass)) {
                return (long) value;
            } else if (String.class.equals(valueClass)) {
                return Long.parseLong(value.toString().trim());
            } else {
                throw new IllegalFormatException(String.format("property '%s' is not a long", value));
            }
        }
        return defaultValue;
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        Object value = get(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Float.class.equals(valueClass)) {
                return (float) value;
            } else if (String.class.equals(valueClass)) {
                return Float.parseFloat(value.toString().trim());
            } else {
                throw new IllegalFormatException(String.format("property '%s' is not a float", value));
            }
        }
        return defaultValue;
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        Object value = get(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Double.class.equals(valueClass)) {
                return (double) value;
            } else if (String.class.equals(valueClass)) {
                return Double.parseDouble(value.toString().trim());
            } else {
                throw new IllegalFormatException(String.format("property '%s' is not a double", value));
            }
        }
        return defaultValue;
    }

    @Nullable
    @Override
    public <T> T get(String key, Function<Object, T> func) {
        Object value = get(key);
        if (Objects.nonNull(value)) {
            return func.apply(value);
        }
        return null;
    }

    @Override
    public <T> T get(String key, Function<Object, T> func, T defaultValue) {
        Object value = get(key);
        if (Objects.nonNull(value)) {
            return func.apply(value);
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T remove(String key) {
        return (T) properties.remove(key);
    }

    @Override
    public Map<String, Object> toMap() {
        return new HashMap<>(properties);
    }

    @Override
    public void clear() {
        properties.clear();
    }

    //-------------------------------------------------------------------------------------------Map
    @Override
    public int size() {
        return properties.size();
    }

    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return properties.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return properties.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return properties.get(key);
    }

    @Override
    public Object remove(Object key) {
        return properties.remove(key);
    }

    @Override
    public Set<String> keySet() {
        return properties.keySet();
    }

    @Override
    public Collection<Object> values() {
        return properties.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return properties.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MapConfigurationProperties)) {
            return false;
        }
        MapConfigurationProperties that = (MapConfigurationProperties) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties);
    }

    @Override
    public String toString() {
        return "MapConfigurationProperties{" +
                "properties=" + properties +
                '}';
    }
}
