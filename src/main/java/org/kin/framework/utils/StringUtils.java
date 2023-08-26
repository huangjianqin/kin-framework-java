package org.kin.framework.utils;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author huangjianqin
 * @date 2018/5/25
 */
public class StringUtils {
    private static final String DELIMITER = ",";
    private static final String KV_DELIMITER = "=";
    private static final String ALL_MATCH_PATTERN = "*";
    /** 常用字符 */
    private static final char[] CHARS = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890~!@#$%^&*()-=_+[]{};:,./<>?|".toCharArray();
    /** 十六进制字符 */
    private static final String HEX_STRING = "0123456789ABCDEF";
    /** 十六进制字符 */
    private static final char[] HEX_CHARS = HEX_STRING.toCharArray();

    /**
     * 字符串是否为空(null or 空串)
     */
    public static boolean isBlank(String s) {
        return s == null || "".equals(s.trim());
    }

    /**
     * 字符串是否非空(null or 空串)
     */
    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    /**
     * 字符串反转
     */
    public static String reverse(String s) {
        if (isNotBlank(s)) {
            return new StringBuilder(s).reverse().toString();
        }
        return s;
    }

    /**
     * 数组格式化
     */
    @SafeVarargs
    public static <E> String mkString(E... contents) {
        return mkString(DELIMITER, contents);
    }

    /**
     * 数组格式化
     */
    @SafeVarargs
    public static <E> String mkString(String delimiter, E... contents) {
        return mkString(delimiter, CollectionUtils.toList(contents), Object::toString);
    }

    /**
     * 数组格式化
     */
    @SafeVarargs
    public static <E> String mkString(Function<E, String> mapper, E... contents) {
        return mkString(DELIMITER, CollectionUtils.toList(contents), mapper);
    }

    /**
     * 数组格式化
     */
    @SafeVarargs
    public static <E> String mkString(String delimiter, Function<E, String> mapper, E... contents) {
        return mkString(delimiter, CollectionUtils.toList(contents), mapper);
    }

    /**
     * 集合格式化
     */
    public static <E> String mkString(Collection<E> collection) {
        return mkString(DELIMITER, collection, Object::toString);
    }

    /**
     * 集合格式化
     */
    public static <E> String mkString(Collection<E> collection, Function<E, String> mapper) {
        return mkString(DELIMITER, collection, mapper);
    }

    /**
     * 集合格式化
     */
    public static <E> String mkString(String delimiter, Collection<E> collection) {
        return mkString(delimiter, collection, Object::toString);
    }

    /**
     * 集合格式化
     */
    public static <E> String mkString(String delimiter, Collection<E> collection, Function<E, String> mapper) {
        if (CollectionUtils.isNonEmpty(collection)) {
            return collection
                    .stream()
                    .map(mapper)
                    .collect(Collectors.joining(delimiter));
        }
        return "";
    }

    /**
     * map格式化
     */
    public static <K, V> String mkString(Map<K, V> map) {
        return mkString(DELIMITER, map);
    }

    /**
     * map格式化
     */
    public static <K, V> String mkString(String delimiter,
                                         Map<K, V> map) {
        return mkString(delimiter, map, KV_DELIMITER, Object::toString, Object::toString);
    }

    /**
     * map格式化
     */
    public static <K, V> String mkString(Map<K, V> map,
                                         String kvDelimiter,
                                         Function<K, String> keyMapper,
                                         Function<V, String> valueMapper) {
        return mkString(DELIMITER, map, kvDelimiter, keyMapper, valueMapper);
    }

    /**
     * map格式化
     */
    public static <K, V> String mkString(String delimiter,
                                         Map<K, V> map,
                                         String kvDelimiter,
                                         Function<K, String> keyMapper,
                                         Function<V, String> valueMapper) {
        if (CollectionUtils.isNonEmpty(map)) {
            return map.entrySet()
                    .stream()
                    .map(entry -> keyMapper.apply(entry.getKey()).concat(kvDelimiter)
                            .concat(valueMapper.apply(entry.getValue())))
                    .collect(Collectors.joining(delimiter));
        }
        return "";
    }

    /**
     * 首字母大写
     */
    public static String firstUpperCase(String s) {
        char[] chars = s.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] = (char) (chars[0] - 32);
        }
        return new String(chars);
    }

    /**
     * 首字母小写
     */
    public static String firstLowerCase(String s) {
        char[] chars = s.toCharArray();
        if (chars[0] >= 'A' && chars[0] <= 'Z') {
            chars[0] = (char) (chars[0] + 32);
        }
        return new String(chars);
    }

    /**
     * 字符串转16进制字符串
     */
    public static String str2HexStr(String str) {
        StringBuilder sb = new StringBuilder();
        byte[] bs = str.getBytes();
        int bit;
        for (byte b : bs) {
            bit = (b & 0x0f0) >> 4;
            sb.append(HEX_CHARS[bit]);
            bit = b & 0x0f;
            sb.append(HEX_CHARS[bit]);
        }
        return sb.toString().trim();
    }

    /**
     * 16进制字符串转字符串
     */
    public static String hexStr2Str(String hexStr) {
        return new String(hexStr2Bytes(hexStr), StandardCharsets.UTF_8);
    }

    /**
     * 16进制字符串转bytes
     */
    public static byte[] hexStr2Bytes(String hexStr) {
        char[] hexChars = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = HEX_STRING.indexOf(hexChars[2 * i]) * 16;
            n += HEX_STRING.indexOf(hexChars[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return bytes;
    }

    /**
     * str移除指定后缀suffix
     */
    public static String removeSuffix(String origin, String suffix) {
        if (!isBlank(origin) && !isBlank(suffix)) {
            if (origin.endsWith(suffix)) {
                return origin.substring(0, origin.length() - suffix.length());
            } else {
                return origin;
            }
        } else {
            return origin;
        }
    }

    /**
     * 判断字符串是否为数字
     */
    public static boolean isNumeric(String str) {
        char[] chars = str.toCharArray();
        int idx = 0;
        //判断第一个char是否是-, 因为存在可能是负数
        char first = chars[idx];
        if (first == '-') {
            idx = 1;
        }

        //是否已遍历到'.'
        boolean everPoint = false;
        for (int i = idx; i < chars.length; i++) {
            if (Character.isDigit(chars[i])) {
                continue;
            }

            if (chars[i] == '.' && !everPoint) {
                //首次'.'
                everPoint = true;
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * 字符串匹配, 以*作为通配符
     *
     * @param pattern   字符串模板
     * @param s         需要匹配的字符串
     * @return 匹配结果
     */
    public static boolean match(String pattern, String s) {
        pattern = pattern.trim();
        s = s.trim();

        //"AB"
        if (!pattern.contains(ALL_MATCH_PATTERN)) {
            return pattern.equals(s.trim());
        }
        //"*", match all
        if (pattern.equals(ALL_MATCH_PATTERN)) {
            return true;
        }

        String[] split = pattern.split("\\" + ALL_MATCH_PATTERN);

        if (split.length == 1) {
            //"A*", prefix match.
            return s.startsWith(split[0]);
        } else if (split.length == 2) {
            //"*A", postfix match.
            if (StringUtils.isBlank(split[0])) {
                return s.endsWith(split[1]);
            }
            return s.startsWith(split[0]) && s.endsWith(split[1]);
        }

        return false;
    }

    /**
     * 随机生成长度为{@code len}的字符串
     *
     * @param len 字符串长度
     * @return 字符串
     */
    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int index = ThreadLocalRandom.current().nextInt(CHARS.length);
            sb.append(CHARS[index]);
        }

        return sb.toString();
    }

    /**
     * if s1 is null and s2 is null, then return true
     *
     * @param s1 str1
     * @param s2 str2
     * @return equals
     */
    public static boolean isEquals(@Nullable String s1,
                                   @Nullable String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }
}
