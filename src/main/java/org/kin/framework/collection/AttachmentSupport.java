package org.kin.framework.collection;

import javax.annotation.Nullable;
import java.util.Map;

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
     * 获取attachment value
     *
     * @param key attachment key
     * @return attachment value
     */
    @Nullable
    <T> T attachment(String key);

    /**
     * 获取attachment value, 如果不存在则取{@code defaultValue}
     *
     * @param key          attachment key
     * @param defaultValue 默认attachment value
     * @return attachment value
     */
    <T> T attachment(String key, T defaultValue);

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
