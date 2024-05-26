package org.kin.framework.utils;

/**
 * @author huangjianqin
 * @date 2024/5/22
 */
public class HmacUtilsTest {
    public static void main(String[] args) {
        String appKey = "kin";
        String data = "kin1234567890";
        System.out.println(HmacUtils.encryptAsHexWithSHA256(data, appKey));
        System.out.println(HmacUtils.encryptAsBase64WithSHA256(data, appKey));
        System.out.println("----------");
        System.out.println(HmacUtils.encryptAsHexWithSHA512(data, appKey));
        System.out.println(HmacUtils.encryptAsBase64WithSHA512(data, appKey));
    }
}
