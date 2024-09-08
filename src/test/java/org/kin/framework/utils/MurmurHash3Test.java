package org.kin.framework.utils;

import java.util.Arrays;

/**
 * @author huangjianqin
 * @date 2024/9/8
 */
public class MurmurHash3Test {
    public static void main(String[] args) {
//        String str = StringUtils.randomString(32);
        String str = "HS;AZra04>]HSOJ.@9#8{jQY}%72]Qnb";
        System.out.println(str);
        System.out.println(MurmurHash3.hash32(str));
        System.out.println(MurmurHash3.hash64(str));
        System.out.println(Arrays.toString(MurmurHash3.hash128(str)));
    }
}
