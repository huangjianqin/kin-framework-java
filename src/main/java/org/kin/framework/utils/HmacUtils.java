package org.kin.framework.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Hash-based Message Authentication Code(HMAC)
 * <p>
 * he output of the SHA-256 algorithm is then compared with the HMAC value that was generated using the secret key and the message to be authenticated
 *
 * @author huangjianqin
 * @date 2024/5/22
 */
public final class HmacUtils {
    /** SHA256算法 */
    private static final String HMAC_SHA256 = "HmacSHA256";
    /** SHA512算法 */
    private static final String HMAC_SHA512 = "HmacSHA512";

    private HmacUtils() {
    }

    /**
     * 基于HmacSHA256加密
     *
     * @param data 数据
     * @param key  密钥
     * @return 加密后内容(十六进制)
     */
    public static String encryptAsHexWithSHA256(String data, String key) {
        return SecyCodec.HEX.encode(encrypt(data, key, HMAC_SHA256));
    }

    /**
     * 基于HmacSHA256加密
     *
     * @param data 数据
     * @param key  密钥
     * @return 加密后内容(Base64)
     */
    public static String encryptAsBase64WithSHA256(String data, String key) {
        return SecyCodec.BASE64.encode(encrypt(data, key, HMAC_SHA256));
    }

    /**
     * 基于HmacSHA512加密
     *
     * @param data 数据
     * @param key  密钥
     * @return 加密后内容(十六进制)
     */
    public static String encryptAsHexWithSHA512(String data, String key) {
        return SecyCodec.HEX.encode(encrypt(data, key, HMAC_SHA512));
    }

    /**
     * 基于HmacSHA512加密
     *
     * @param data 数据
     * @param key  密钥
     * @return 加密后内容(Base64)
     */
    public static String encryptAsBase64WithSHA512(String data, String key) {
        return SecyCodec.BASE64.encode(encrypt(data, key, HMAC_SHA512));
    }

    /**
     * 加密
     *
     * @param data      数据
     * @param key       密钥
     * @param algorithm 算法
     * @return 加密后内容(十六进制)
     */
    private static byte[] encrypt(String data, String key, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            mac.init(secretKeySpec);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            ExceptionUtils.throwExt(e);
        }

        return null;
    }

}
