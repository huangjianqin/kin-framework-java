package org.kin.framework.utils;

/**
 * @author huangjianqin
 * @date 2021/11/22
 */
public final class Maths {
    private Maths() {
    }

    /**
     * 计算r=log2(M)
     */
    public static double log2(double m) {
        return log(m, 2);
    }

    /**
     * 计算r=logN(M)
     */
    public static double log(double n, double m) {
        return Math.log(m) / Math.log(n);
    }

    /**
     * 判断{@code val}是否是2^N次方
     */
    public static boolean isPower2(int val) {
        return (val & -val) == val;
    }

    /**
     * 最接近且与大于等于{@code val}的2^n次方整形
     */
    public static int round2Power2(int val) {
        //2^30=1073741824
        if (val > (1 << 30)) {
            throw new IllegalArgumentException(String.format("no larger power of 2 integer for %d since it exceeds 2^30", val));
        } else if (val < 0) {
            throw new IllegalArgumentException("expect positive integer");
        } else {
            if (val <= 2) {
                return val;
            }

            return 1 << 32 - Integer.numberOfLeadingZeros(val - 1);
        }
    }

    /**
     * 最接近且与大于等于{@code val}的2^n次方整形
     */
    public static int ceil2Power2(int val) {
        return round2Power2(val);
    }

    /**
     * 最接近且与小于等于{@code val}的2^n次方整形
     */
    public static int floor2Power2(int val) {
        if (val < 0) {
            throw new IllegalArgumentException("expect positive integer");
        }

        if (val <= 2) {
            return val;
        }

        return Integer.highestOneBit(val);
    }

    /**
     * 判断{@code val}是否是2^N次方
     */
    public static boolean isPower2(long val) {
        return (val & -val) == val;
    }

    /**
     * 最接近且与大于等于{@code val}的2^n次方整形
     */
    public static long round2Power2(long val) {
        //2^62=4611686018427387904
        if (val > 4611686018427387904L) {
            throw new IllegalArgumentException(String.format("no larger power of 2 integer for %d since it exceeds 2^62", val));
        } else if (val < 0) {
            throw new IllegalArgumentException("expect positive long");
        } else {
            if (val <= 2) {
                return val;
            }

            // 如果是已经是2^n, 则-1
            val--;
            val |= val >> 1;
            val |= val >> 2;
            val |= val >> 4;
            val |= val >> 8;
            val |= val >> 16;
            val |= val >> 32;
            return val + 1;
        }
    }

    /**
     * 最接近且与大于等于{@code val}的2^n次方整形
     */
    public static long ceil2Power2(long val) {
        return round2Power2(val);
    }

    /**
     * 最接近且与小于等于{@code val}的2^n次方整形
     */
    public static long floor2Power2(long val) {
        if (val < 0) {
            throw new IllegalArgumentException("expect positive long");
        }

        if (val <= 2) {
            return val;
        }

        return Long.highestOneBit(val);
    }
}
