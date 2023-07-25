package org.kin.framework.collection;

import org.kin.framework.utils.IllegalFormatException;
import org.kin.framework.utils.StringUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2023/6/14
 */
public class AttachmentMap implements AttachmentSupport {
    /** attachments */
    private final Map<String, Object> attachments;

    public AttachmentMap() {
        this(Collections.emptyMap());
    }

    public AttachmentMap(Map<String, ?> attachments) {
        this.attachments = new HashMap<>(attachments);
    }

    public AttachmentMap(AttachmentMap other) {
        this.attachments = new HashMap<>(other.attachments);
    }

    @Override
    public void attachMany(Map<String, ?> attachments) {
        this.attachments.putAll(attachments);
    }

    @Override
    public void attachMany(AttachmentMap other) {
        this.attachments.putAll(other.attachments);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T attach(String key, Object obj) {
        return (T) attachments.put(key, obj);
    }

    /**
     * 是否存在attachment key
     *
     * @param key attachment key
     * @return true表示存在attachment key
     */
    public boolean hasAttachment(String key) {
        return attachments.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T attachment(String key) {
        return (T) attachments.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T attachment(String key, T defaultValue) {
        return (T) attachments.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean boolAttachment(String key, boolean defaultValue) {
        Object value = attachment(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Boolean.class.equals(valueClass) ||
                    Boolean.TYPE.equals(valueClass)) {
                return (boolean) value;
            } else if (String.class.equals(valueClass)) {
                String valueStr = (String) value;
                if (StringUtils.isNumeric(valueStr)) {
                    return Long.parseLong(valueStr) > 0;
                } else {
                    return Boolean.parseBoolean(value.toString());
                }
            } else {
                throw new IllegalFormatException(String.format("attachment '%s' is not a boolean", value));
            }
        }
        return defaultValue;
    }

    @Override
    public byte byteAttachment(String key, byte defaultValue) {
        Object value = attachment(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Byte.class.equals(valueClass) ||
                    Byte.TYPE.equals(valueClass)) {
                return (byte) value;
            } else if (String.class.equals(valueClass)) {
                return Byte.parseByte(value.toString());
            } else {
                throw new IllegalFormatException(String.format("attachment '%s' is not a byte", value));
            }
        }
        return defaultValue;
    }

    @Override
    public short shortAttachment(String key, short defaultValue) {
        Object value = attachment(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Short.class.equals(valueClass) ||
                    Short.TYPE.equals(valueClass)) {
                return (byte) value;
            } else if (String.class.equals(valueClass)) {
                return Short.parseShort(value.toString());
            } else {
                throw new IllegalFormatException(String.format("attachment '%s' is not a short", value));
            }
        }
        return defaultValue;
    }

    @Override
    public int intAttachment(String key, int defaultValue) {
        Object value = attachment(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Integer.class.equals(valueClass) ||
                    Integer.TYPE.equals(valueClass)) {
                return (byte) value;
            } else if (String.class.equals(valueClass)) {
                return Integer.parseInt(value.toString());
            } else {
                throw new IllegalFormatException(String.format("attachment '%s' is not a integer", value));
            }
        }
        return defaultValue;
    }

    @Override
    public long longAttachment(String key, long defaultValue) {
        Object value = attachment(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Long.class.equals(valueClass) ||
                    Long.TYPE.equals(valueClass)) {
                return (byte) value;
            } else if (String.class.equals(valueClass)) {
                return Long.parseLong(value.toString());
            } else {
                throw new IllegalFormatException(String.format("attachment '%s' is not a long", value));
            }
        }
        return defaultValue;
    }

    @Override
    public float floatAttachment(String key, float defaultValue) {
        Object value = attachment(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Float.class.equals(valueClass) ||
                    Float.TYPE.equals(valueClass)) {
                return (byte) value;
            } else if (String.class.equals(valueClass)) {
                return Float.parseFloat(value.toString());
            } else {
                throw new IllegalFormatException(String.format("attachment '%s' is not a float", value));
            }
        }
        return defaultValue;
    }

    @Override
    public double doubleAttachment(String key, double defaultValue) {
        Object value = attachment(key);
        if (Objects.nonNull(value)) {
            Class<?> valueClass = value.getClass();
            if (Double.class.equals(valueClass) ||
                    Double.TYPE.equals(valueClass)) {
                return (byte) value;
            } else if (String.class.equals(valueClass)) {
                return Double.parseDouble(value.toString());
            } else {
                throw new IllegalFormatException(String.format("attachment '%s' is not a double", value));
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T detach(String key) {
        return (T) attachments.remove(key);
    }

    @Override
    public Map<String, Object> attachments() {
        return attachments;
    }

    @Override
    public void clear() {
        attachments.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AttachmentMap that = (AttachmentMap) o;
        return Objects.equals(attachments, that.attachments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attachments);
    }

    @Override
    public String toString() {
        return "AttachmentMap{" +
                "attachments=" + attachments +
                '}';
    }
}
