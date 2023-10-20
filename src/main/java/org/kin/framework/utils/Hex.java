package org.kin.framework.utils;

import java.nio.charset.StandardCharsets;

/**
 * 16进制转换工具类
 * @author huangjianqin
 * @date 2023/10/18
 */
public final class Hex {
    /** 16进制字符(小写) */
    private static final char[] DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    /** 16进制字符(大写) */
    private static final char[] DIGITS_UPPER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 16进制编码
     */
    public static String encode(String str) {
        return encode(str, false);
    }

    /**
     * 16进制编码
     */
    public static String encode(String str, boolean toLowerCase) {
        return encode(str.getBytes(StandardCharsets.UTF_8), toLowerCase);
    }

    /**
     * 16进制编码
     */
    public static String encode(byte[] data) {
        return encode(data, false);
    }

    /**
     * 16进制编码
     */
    public static String encode(byte[] data, boolean toLowerCase) {
        return new String(encodeHex(data, toLowerCase? DIGITS_LOWER: DIGITS_UPPER));
    }

    /**
     * 16进制编码
     */
    private static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        int i = 0;

        for(int j = 0; i < l; ++i) {
            out[j++] = toDigits[(240 & data[i]) >>> 4];
            out[j++] = toDigits[15 & data[i]];
        }

        return out;
    }

    /**
     * 16进制解码
     */
    public static byte[] decode(String str) {
        return decodeHex(str.toCharArray());
    }

    /**
     * 16进制解码
     */
    public static String decode2String(String str) {
        return new String(decodeHex(str.toCharArray()), StandardCharsets.UTF_8);
    }

    /**
     * 16进制解码
     */
    private static byte[] decodeHex(char[] data){
        int len = data.length;
        if ((len & 1) != 0) {
            throw new DecodeException("odd number of characters");
        } else {
            byte[] out = new byte[len >> 1];
            int i = 0;

            for(int j = 0; j < len; ++i) {
                int f = toDigit(data[j], j) << 4;
                ++j;
                f |= toDigit(data[j], j);
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

    private Hex() {
    }
}
