package org.kin.framework.utils;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA非对称加密算法
 * 使用RSA对SHA256再次加密
 * 可以理解为增强型SHA256
 *
 * @author huangjianqin
 * @date 2024/5/22
 */
public final class RSAUtils {
    /** 签名算法 */
    private static final String ALGORITHM = "SHA256WithRSA";

    private RSAUtils() {
    }

    /**
     * 加密
     *
     * @param data        数据
     * @param priKeyBytes 密钥
     * @return 加密后内容(十六进制)
     */
    public static String encryptAsHex(String data, byte[] priKeyBytes) {
        return SecyCodec.HEX.encode(encrypt(data, priKeyBytes));
    }

    /**
     * 加密
     *
     * @param data        数据
     * @param priKeyBytes 密钥
     * @return 加密后内容(Base64)
     */
    public static String encryptAsBase64(String data, byte[] priKeyBytes) {
        return SecyCodec.BASE64.encode(encrypt(data, priKeyBytes));
    }

    /**
     * 加密
     *
     * @param data        数据
     * @param priKeyBytes 密钥
     * @return 加密后内容(bytes)
     */
    private static byte[] encrypt(String data, byte[] priKeyBytes) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(priKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Signature sig = Signature.getInstance(ALGORITHM);
            sig.initSign(privateKey);
            sig.update(SHA256.common().digest(data));
            return sig.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | SignatureException e) {
            ExceptionUtils.throwExt(e);
        }

        return null;
    }

    /**
     * 验签
     *
     * @param data          原始数据
     * @param pubKeyBytes   公钥
     * @param encryptedData 私钥加密后的数据(Hex)
     * @return 验签结果, true=验签成功
     */
    public static boolean verifyHex(String data, byte[] pubKeyBytes, String encryptedData) {
        return verify(data, pubKeyBytes, SecyCodec.HEX.decode(encryptedData));
    }

    /**
     * 验签
     *
     * @param data          原始数据
     * @param pubKeyBytes   公钥
     * @param encryptedData 私钥加密后的数据(Base64)
     * @return 验签结果, true=验签成功
     */
    public static boolean verifyBase64(String data, byte[] pubKeyBytes, String encryptedData) {
        return verify(data, pubKeyBytes, SecyCodec.BASE64.decode(encryptedData));
    }

    /**
     * 验签
     *
     * @param data               原始数据
     * @param pubKeyBytes        公钥
     * @param encryptedDataBytes 私钥加密后的数据
     * @return 验签结果, true=验签成功
     */
    private static boolean verify(String data, byte[] pubKeyBytes, byte[] encryptedDataBytes) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Signature sig = Signature.getInstance(ALGORITHM);

            // 验证签名
            sig.initVerify(publicKey);
            sig.update(SHA256.common().digest(data));
            return sig.verify(encryptedDataBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            ExceptionUtils.throwExt(e);
        }

        return false;
    }
}
