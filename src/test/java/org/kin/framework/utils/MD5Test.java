package org.kin.framework.utils;

/**
 * @author huangjianqin
 * @date 2022/7/2
 */
public class MD5Test {
    public static void main(String[] args) {
        //全量计算
        String target = "kin1234567890";
        System.out.println(MD5.common().digestAsHex(target));

        //增量计算
        for (char c : target.toCharArray()) {
            MD5.current().update(c + "");
        }
        System.out.println(MD5.current().digestAsHex());
    }
}
