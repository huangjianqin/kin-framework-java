package org.kin.framework.collection;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2023/6/14
 */
public class AttachmentMap implements AttachmentSupport{
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

    /**
     * 批量attach
     *
     * @param attachments attachments
     */
    @Override
    public void attachMany(Map<String, ?> attachments) {
        this.attachments.putAll(attachments);
    }

    /**
     * 批量attach
     *
     * @param other attachments
     */
    @Override
    public void attachMany(AttachmentMap other) {
        this.attachments.putAll(other.attachments);
    }

    /**
     * attach
     *
     * @param key attachment key
     * @param obj attachment value
     * @return this
     */
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

    /**
     * 获取attachment value
     *
     * @param key attachment key
     * @return attachment value
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T attachment(String key) {
        return (T) attachments.get(key);
    }

    /**
     * 获取attachment value, 如果不存在则取{@code defaultValue}
     *
     * @param key          attachment key
     * @param defaultValue 默认attachment value
     * @return attachment value
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T attachment(String key, T defaultValue) {
        return (T) attachments.getOrDefault(key, defaultValue);
    }

    /**
     * 移除attachment
     *
     * @param key attachment key
     * @return attachment value if exists
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T detach(String key) {
        return (T)attachments.remove(key);
    }

    /**
     * 返回所有attachment
     * @return  所有attachment
     */
    @Override
    public Map<String, Object> attachments(){
        return attachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
