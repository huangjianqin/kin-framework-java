package org.kin.framework.utils;

/**
 * @author huangjianqin
 * @date 2021/11/22
 */
public class MathsTest {
    public static void main(String[] args) {
        System.out.println("---------------------------log2");
        System.out.println(Maths.log2(1025));
        System.out.println(Maths.log2(1024));
        System.out.println(Maths.log2(1023));

        System.out.println("---------------------------log");
        System.out.println(Maths.log(5, 1));
        System.out.println(Maths.log(5, 25));

        System.out.println("---------------------------isPower2");
        System.out.println(Maths.isPower2(4));

        System.out.println("---------------------------ceil2Power2");
        System.out.println(Maths.ceil2Power2(1));
        System.out.println(Maths.ceil2Power2(2));
        System.out.println(Maths.ceil2Power2(3));
        System.out.println(Maths.ceil2Power2(4));
        System.out.println(Maths.ceil2Power2(500));
        System.out.println(Maths.ceil2Power2(1000));
//        System.out.println(Maths.ceil2Power2(Integer.MAX_VALUE));

        System.out.println("---------------------------floor2Power2");
        System.out.println(Maths.floor2Power2(1));
        System.out.println(Maths.floor2Power2(2));
        System.out.println(Maths.floor2Power2(4));
        System.out.println(Maths.floor2Power2(5));
        System.out.println(Maths.floor2Power2(515));
        System.out.println(Maths.floor2Power2(1025));
        System.out.println(Maths.floor2Power2(Integer.MAX_VALUE));

        System.out.println("---------------------------ceil2Power2(long)");
        System.out.println(Maths.ceil2Power2(1L));
        System.out.println(Maths.ceil2Power2(2L));
        System.out.println(Maths.ceil2Power2(3L));
        System.out.println(Maths.ceil2Power2(4L));
        System.out.println(Maths.ceil2Power2(500L));
        System.out.println(Maths.ceil2Power2(1000L));
        System.out.println(Maths.ceil2Power2(Long.valueOf(Integer.MAX_VALUE)));
//        System.out.println(Maths.ceil2Power2(Long.MAX_VALUE));

        System.out.println("---------------------------floor2Power2(long)");
        System.out.println(Maths.floor2Power2(1L));
        System.out.println(Maths.floor2Power2(2L));
        System.out.println(Maths.floor2Power2(4L));
        System.out.println(Maths.floor2Power2(5L));
        System.out.println(Maths.floor2Power2(515L));
        System.out.println(Maths.floor2Power2(1025L));
        System.out.println(Maths.floor2Power2(Long.valueOf(Integer.MAX_VALUE)));
        System.out.println(Maths.floor2Power2(Long.MAX_VALUE));
    }
}
