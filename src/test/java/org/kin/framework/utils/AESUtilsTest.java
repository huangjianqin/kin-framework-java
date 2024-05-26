package org.kin.framework.utils;

/**
 * @author huangjianqin
 * @date 2023/10/18
 */
public class AESUtilsTest {
    public static void main(String[] args) {
        //需加密内容
        String content = "F45PksGt0RpXmeGMeO8H86qPabJsUjiX";
        //password
        String salt = "1U1T0LHS";

        System.out.println("------------------ECB------------------");
//        String ecbHexEncryption = "5879fe39515593b6e13b14d345168579521b43cfd2042521d9538a8a61c897082068968640b34bc80b70199131f235b2";
        String ecbHexEncryption = "5879FE39515593B6E13B14D345168579521B43CFD2042521D9538A8A61C897082068968640B34BC80B70199131F235B2";
        String ecbBase64Encryption = "WHn+OVFVk7bhOxTTRRaFeVIbQ8/SBCUh2VOKimHIlwggaJaGQLNLyAtwGZEx8jWy";

        System.out.println(AESUtils.encryptAsHexWithECB(content, salt));
        System.out.println(AESUtils.decryptHexWithECB(ecbHexEncryption, salt));
        System.out.println("---");

        System.out.println(AESUtils.encryptAsBase64WithECB(content, salt));
        System.out.println(AESUtils.decryptBase64WithECB(ecbBase64Encryption, salt));
        System.out.println("---");

        System.out.println();
        System.out.println("------------------GCM------------------");
        String gcmBase64Encryption = "RCvH7RQe41NvZrp4kokByFhSmO+CHf8g9zS9H22RS04z7y1plKp/f7wV7HdX8f0NInTKZbLxLSli6u1o";
        System.out.println(AESUtils.encryptAsBase64WithZeroGCM(content, salt));
        System.out.println(AESUtils.decryptBase64WithZeroGCM(gcmBase64Encryption, salt));
        System.out.println("---");
    }
}
