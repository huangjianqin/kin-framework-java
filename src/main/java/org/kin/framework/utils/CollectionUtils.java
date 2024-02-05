package org.kin.framework.utils;

import org.kin.framework.collection.AttachmentMap;

import java.util.*;

/**
 * @author huangjianqin
 * @date 2019/7/6
 */
public class CollectionUtils {
    /** empty {@link Enumeration}实现 */
    private static final Enumeration<Object> EMPTY_ENUMERATION = Collections.enumeration(Collections.emptyList());

    public static <E> boolean isEmpty(Collection<E> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <E> boolean isNonEmpty(Collection<E> collection) {
        return !isEmpty(collection);
    }

    public static <E> boolean isEmpty(E[] array) {
        return array == null || array.length <= 0;
    }

    public static <E> boolean isNonEmpty(E[] array) {
        return !isEmpty(array);
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    public static <K, V> boolean isNonEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }

    public static <ITEM> List<ITEM> toList(ITEM[] array) {
        return Arrays.asList(array);
    }

    /** 判断两集合是否一致 */
    public static <T> boolean isSame(Collection<T> source, Collection<T> other) {
        return source.size() == other.size() && source.containsAll(other) && other.containsAll(source);
    }

    /**
     * 返回empty {@link Enumeration}实现
     *
     * @return empty {@link Enumeration}实现
     */
    @SuppressWarnings("unchecked")
    public static <T> Enumeration<T> emptyEnumeration() {
        return (Enumeration<T>) EMPTY_ENUMERATION;
    }

    public static <E> boolean isEmpty(AttachmentMap attachmentMap) {
        return attachmentMap == null || attachmentMap.isEmpty();
    }

    public static <E> boolean isNonEmpty(AttachmentMap attachmentMap) {
        return !isEmpty(attachmentMap);
    }
}
