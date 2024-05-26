package org.kin.framework.utils;

import java.nio.charset.StandardCharsets;

/**
 * 十六进制转换工具类
 * @author huangjianqin
 * @date 2023/10/18
 */
public final class HexUtils {

    private HexUtils() {
    }

    /**
     * 十六进制编码
     */
    public static String encode(String str) {
        return encode(str, false);
    }

    /**
     * 十六进制编码
     */
    public static String encode(String str, boolean toLowerCase) {
        return encode(str.getBytes(StandardCharsets.UTF_8), toLowerCase);
    }

    /**
     * 十六进制编码
     */
    public static String encode(byte[] bytes) {
        return encode(bytes, false);
    }

    /**
     * 十六进制编码
     */
    public static String encode(byte[] bytes, boolean toLowerCase) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
        }
        return toLowerCase ? sb.toString() : sb.toString().toUpperCase();
    }
    /**
     * 十六进制解码
     */
    public static byte[] decode(String str) {
        return decodeHex(str.toCharArray());
    }

    /**
     * 十六进制解码
     */
    public static String decode2String(String str) {
        return new String(decodeHex(str.toCharArray()), StandardCharsets.UTF_8);
    }

    /**
     * 十六进制解码
     */
    private static byte[] decodeHex(char[] chars) {
        int len = chars.length;
        if ((len & 1) != 0) {
            throw new DecodeException("odd number of characters");
        } else {
            byte[] out = new byte[len >> 1];
            int i = 0;

            for(int j = 0; j < len; ++i) {
                int f = toDigit(chars[j], j) << 4;
                ++j;
                f |= toDigit(chars[j], j);
                ++j;
                out[i] = (byte)(f & 255);
            }

            return out;
        }
    }

    private static int toDigit(char ch, int index){
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new DecodeException("illegal hexadecimal character " + ch + " at index " + index);
        } else {
            return digit;
        }
    }
}
