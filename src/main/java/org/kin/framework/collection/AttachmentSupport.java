package org.kin.framework.collection;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

/**
 * @author huangjianqin
 * @date 2023/6/25
 */
public interface AttachmentSupport {
    /**
     * 批量attach
     *
     * @param attachments attachments
     */
    void attachMany(Map<String, ?> attachments);

    /**
     * 批量attach
     *
     * @param other attachments
     */
    void attachMany(AttachmentMap other);

    /**
     * attach
     *
     * @param key attachment key
     * @param obj attachment value
     * @return this
     */
    @Nullable
    <T> T attach(String key, Object obj);

    /**
     * 是否存在attachment key
     *
     * @param key attachment key
     * @return true表示存在attachment key
     */
    boolean hasAttachment(String key);

    /**
     * 返回attachment value
     *
     * @param key attachment key
     * @return attachment value
     */
    @Nullable
    <T> T attachment(String key);

    /**
     * 返回attachment value, 如果不存在则取{@code defaultValue}
     *
     * @param key          attachment key
     * @param defaultValue 默认attachment value
     * @return attachment value
     */
    <T> T attachment(String key, T defaultValue);

    /**
     * 返回attachment boolean value
     *
     * @param key attachment key
     * @return attachment boolean value
     */
    default boolean boolAttachment(String key) {
        return boolAttachment(key, false);
    }

    /**
     * 返回attachment boolean value, 如果不存在则取{@code defaultValue}
     *
     * @param key          attachment key
     * @param defaultValue 默认attachment boolean value
     * @return attachment boolean value
     */
    boolean boolAttachment(String key, boolean defaultValue);

    /**
     * 返回attachment byte value
     *
     * @param key attachment key
     * @return attachment byte value
     */
    default byte byteAttachment(String key) {
        return byteAttachment(key, (byte) 0);
    }

    /**
     * 返回attachment byte value, 如果不存在则取{@code defaultValue}
     *
     * @param key          attachment key
     * @param defaultValue 默认attachment byte value
     * @return attachment byte value
     */
    byte byteAttachment(String key, byte defaultValue);

    /**
     * 返回attachment short value
     *
     * @param key attachment key
     * @return attachment short value
     */
    default short shortAttachment(String key) {
        return shortAttachment(key, (short) 0);
    }

    /**
     * 返回attachment short value, 如果不存在则取{@code defaultValue}
     *
     * @param key          attachment key
     * @param defaultValue 默认attachment short value
     * @return attachment short value
     */
    short shortAttachment(String key, short defaultValue);

    /**
     * 返回attachment int value
     *
     * @param key attachment key
     * @return attachment int value
     */
    default int intAttachment(String key) {
        return intAttachment(key, 0);
    }

    /**
     * 返回attachment int value, 如果不存在则取{@code defaultValue}
     *
     * @param key          attachment key
     * @param defaultValue 默认attachment int value
     * @return attachment int value
     */
    int intAttachment(String key, int defaultValue);

    /**
     * 返回attachment long value
     *
     * @param key attachment key
     * @return attachment long value
     */
    default long longAttachment(String key) {
        return longAttachment(key, 0L);
    }

    /**
     * 返回attachment long value, 如果不存在则取{@code defaultValue}
     *
     * @param key          attachment key
     * @param defaultValue 默认attachment long value
     * @return attachment long value
     */
    long longAttachment(String key, long defaultValue);

    /**
     * 返回attachment float value
     *
     * @param key attachment key
     * @return attachment float value
     */
    default float floatAttachment(String key) {
        return floatAttachment(key, 0F);
    }

    /**
     * 返回attachment float value, 如果不存在则取{@code defaultValue}
     *
     * @param key          attachment key
     * @param defaultValue 默认attachment float value
     * @return attachment float value
     */
    float floatAttachment(String key, float defaultValue);

    /**
     * 返回attachment double value
     *
     * @param key attachment key
     * @return attachment double value
     */
    default double doubleAttachment(String key) {
        return doubleAttachment(key, 0D);
    }

    /**
     * 返回attachment double value, 如果不存在则取{@code defaultValue}
     *
     * @param key          attachment key
     * @param defaultValue 默认attachment double value
     * @return attachment double value
     */
    double doubleAttachment(String key, double defaultValue);

    /**
     * 返回attachment value, 并使用{@code func}进行值转换
     *
     * @param key  attachment key
     * @param func attachment convert function
     * @return attachment value
     */
    @Nullable
    <T> T attachment(String key, Function<Object, T> func);

    /**
     * 返回attachment value, 并使用{@code func}进行值转换, 如果不存在则取{@code defaultValue}
     *
     * @param key          attachment key
     * @param func         attachment convert function
     * @param defaultValue 默认attachment value
     * @return attachment value
     */
    <T> T attachment(String key, Function<Object, T> func, T defaultValue);

    /**
     * 移除attachment
     *
     * @param key attachment key
     * @return attachment value if exists
     */
    @Nullable
    <T> T detach(String key);

    /**
     * 返回所有attachment
     *
     * @return 所有attachment
     */
    Map<String, Object> attachments();

    /**
     * 移除所有attachment
     */
    void clear();
}
