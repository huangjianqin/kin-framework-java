package org.kin.framework.utils;

/**
 * @author huangjianqin
 * @date 2024/5/21
 */
public class SHA256Test {
    public static void main(String[] args) {
        //全量计算
        String target = "kin1234567890";
        System.out.println(SHA256.common().digestAsHex(target));

        //增量计算
        for (char c : target.toCharArray()) {
            SHA256.current().update(c + "");
        }
        System.out.println(SHA256.current().digestAsHex());
    }
}
