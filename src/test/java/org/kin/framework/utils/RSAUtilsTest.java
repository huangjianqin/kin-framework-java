package org.kin.framework.utils;

import java.security.*;

/**
 * @author huangjianqin
 * @date 2024/5/23
 */
public class RSAUtilsTest {
    public static void main(String[] args) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.genKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        String data = "kin1234567890";
        String encryptedData = RSAUtils.encryptAsHex(data, privateKey.getEncoded());
        System.out.println(RSAUtils.verifyHex(data, publicKey.getEncoded(), encryptedData));
    }
}
