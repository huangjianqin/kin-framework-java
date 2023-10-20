package org.kin.framework.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * key要求是16个字节,即128位
 *
 * @author huangjianqin
 * @date 2023/10/17
 */
public final class AESUtils {
    /** AES/ECB/PKCS5Padding加密算法 */
    private static final String ECB_ALGORITHM = "AES/ECB/PKCS5Padding";
    /** AES/ECB/PKCS5Padding加密算法 */
    private static final String CBC_ALGORITHM = "AES/ECB/PKCS5Padding";
    /** AES/ECB/NoPadding加密算法 */
    private static final String ZERO_CBC_ALGORITHM = "AES/ECB/NoPadding";
    /** AES/GCM/NoPadding加密算法 */
    private static final String ZERO_GCM_ALGORITHM = "AES/GCM/NoPadding";
    /** 秘钥算法 */
    private static final String KEY_ALGORITHM = "AES";
    /** 强随机种子算法 */
    private static final String RNG_ALGORITHM = "SHA1PRNG";
    /** iv向量bytes长度 */
    private static final int IV_LEN = 12;
    /** GCM算法tag bytes长度 */
    private static final int GCM_TAG_BIT_LEN = 128;

    /** 秘钥缓存 */
    private static final LoadingCache<String, SecretKeySpec> SECRET_KEY_SPEC_CACHE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(15L, TimeUnit.DAYS)
            .build(new CacheLoader<String, SecretKeySpec>() {
                @Override
                public SecretKeySpec load(String key) throws Exception {
                    String rngAlgorithm = "";
                    String password = key;
                    if (key.contains("-")) {
                        String[] splits = key.split("-");
                        rngAlgorithm = splits[0];
                        password = splits[1];
                    }
                    return getSecretKey(password, rngAlgorithm);
                }
            });

    //------------------------------------------------------------------------------------------------------------------ECB

    /**
     * 基于{@link #ECB_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     *
     * @param content  明文
     * @param password 密码
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String weakEncryptECBBase64(String content,
                                              String password) {
        return encryptECB(content, password, null, Codec.BASE64);
    }

    /**
     * 基于{@link #ECB_ALGORITHM}算法对{@link Codec#BASE64}编码后的密文{@code content}进行解密
     *
     * @param content  {@link Codec#BASE64}编码后的密文
     * @param password 密码
     * @return 明文
     */
    public static String weakDecryptECBBase64(String content,
                                              String password) {
        return decryptECB(content, password, null, Codec.BASE64);
    }

    /**
     * 基于{@link #ECB_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     *
     * @param content  明文
     * @param password 密码
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String weakEncryptECBHex(String content,
                                           String password) {
        return encryptECB(content, password, null, Codec.HEX);
    }

    /**
     * 基于{@link #ECB_ALGORITHM}算法对{@link Codec#HEX}编码后的密文{@code content}进行解密
     *
     * @param content  {@link Codec#HEX}编码后的密文
     * @param password 密码
     * @return 明文
     */
    public static String weakDecryptECBHex(String content,
                                           String password) {
        return decryptECB(content, password, null, Codec.HEX);
    }

    /**
     * 基于{@link #ECB_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  明文
     * @param password 密码
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String encryptECBBase64(String content,
                                          String password) {
        return encryptECB(content, password, RNG_ALGORITHM, Codec.BASE64);
    }

    /**
     * 基于{@link #ECB_ALGORITHM}算法对{@link Codec#BASE64}编码后的密文{@code content}进行解密
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  {@link Codec#BASE64}编码后的密文
     * @param password 密码
     * @return 明文
     */
    public static String decryptECBBase64(String content,
                                          String password) {
        return decryptECB(content, password, RNG_ALGORITHM, Codec.BASE64);
    }

    /**
     * 基于{@link #ECB_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  明文
     * @param password 密码
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String encryptECBHex(String content,
                                       String password) {
        return encryptECB(content, password, RNG_ALGORITHM, Codec.HEX);
    }

    /**
     * 基于{@link #ECB_ALGORITHM}算法对{@link Codec#HEX}编码后的密文{@code content}进行解密
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  {@link Codec#HEX}编码后的密文
     * @param password 密码
     * @return 明文
     */
    public static String decryptECBHex(String content,
                                       String password) {
        return decryptECB(content, password, RNG_ALGORITHM, Codec.HEX);
    }

    //------------------------------------------------------------------------------------------------------------------CBC

    /**
     * 基于{@link #CBC_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String weakEncryptCBCBASE64(String content,
                                              String password,
                                              String iv) {
        return encryptCBC(content, password, null, iv, Codec.BASE64);
    }

    /**
     * 基于{@link #CBC_ALGORITHM}算法对{@link Codec#BASE64}编码后的密文{@code content}进行解密
     *
     * @param content  {@link Codec#BASE64}编码后的密文
     * @param password 密码
     * @param iv       iv向量参数
     * @return 明文
     */
    public static String weakDecryptCBCBASE64(String content,
                                              String password,
                                              String iv) {
        return decryptCBC(content, password, null, iv, Codec.BASE64);
    }

    /**
     * 基于{@link #CBC_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String weakEncryptCBCHex(String content,
                                           String password,
                                           String iv) {
        return encryptCBC(content, password, null, iv, Codec.HEX);
    }

    /**
     * 基于{@link #CBC_ALGORITHM}算法对{@link Codec#HEX}编码后的密文{@code content}进行解密
     *
     * @param content  {@link Codec#HEX}编码后的密文
     * @param password 密码
     * @param iv       iv向量参数
     * @return 明文
     */
    public static String weakDecryptCBCHex(String content,
                                           String password,
                                           String iv) {
        return decryptCBC(content, password, null, iv, Codec.HEX);
    }

    /**
     * 基于{@link #CBC_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String encryptCBCBase64(String content,
                                          String password,
                                          String iv) {
        return encryptCBC(content, password, RNG_ALGORITHM, iv, Codec.BASE64);
    }

    /**
     * 基于{@link #CBC_ALGORITHM}算法对{@link Codec#BASE64}编码后的密文{@code content}进行解密
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  {@link Codec#BASE64}编码后的密文
     * @param password 密码
     * @param iv       iv向量参数
     * @return 明文
     */
    public static String decryptCBCBase64(String content,
                                          String password,
                                          String iv) {
        return decryptCBC(content, password, RNG_ALGORITHM, iv, Codec.BASE64);
    }

    /**
     * 基于{@link #CBC_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String encryptCBCHex(String content,
                                       String password,
                                       String iv) {
        return encryptCBC(content, password, RNG_ALGORITHM, iv, Codec.HEX);
    }

    /**
     * 基于{@link #CBC_ALGORITHM}算法对{@link Codec#HEX}编码后的密文{@code content}进行解密
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  {@link Codec#HEX}编码后的密文
     * @param password 密码
     * @param iv       iv向量参数
     * @return 明文
     */
    public static String decryptCBCHex(String content,
                                       String password,
                                       String iv) {
        return decryptCBC(content, password, RNG_ALGORITHM, iv, Codec.HEX);
    }

    //------------------------------------------------------------------------------------------------------------------zero CBC

    /**
     * 基于{@link #ZERO_CBC_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#BASE64}对密文进行编码
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#BASE64}编码后的密文
     */
    public static String weakEncryptZeroCBCBase64(String content,
                                                  String password,
                                                  String iv) {
        return encryptZeroCBC(content, password, null, iv, Codec.BASE64);
    }

    /**
     * 基于{@link #ZERO_CBC_ALGORITHM}算法对{@link Codec#BASE64}编码后的密文{@code content}进行解密
     *
     * @param content  {@link Codec#BASE64}编码后的密文
     * @param password 密码
     * @param iv       iv向量参数
     * @return 明文
     */
    public static String weakDecryptZeroCBCBase64(String content,
                                                  String password,
                                                  String iv) {
        return decryptZeroCBC(content, password, null, iv, Codec.BASE64);
    }

    /**
     * 基于{@link #ZERO_CBC_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String weakEncryptZeroCBCHex(String content,
                                               String password,
                                               String iv) {
        return encryptZeroCBC(content, password, null, iv, Codec.HEX);
    }

    /**
     * 基于{@link #ZERO_CBC_ALGORITHM}算法对{@link Codec#HEX}编码后的密文{@code content}进行解密
     *
     * @param content  {@link Codec#HEX}编码后的密文
     * @param password 密码
     * @param iv       iv向量参数
     * @return 明文
     */
    public static String weakDecryptZeroCBCHex(String content,
                                               String password,
                                               String iv) {
        return decryptZeroCBC(content, password, null, iv, Codec.HEX);
    }

    /**
     * 基于{@link #ZERO_CBC_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#BASE64}对密文进行编码
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#BASE64}编码后的密文
     */
    public static String encryptZeroCBCBase64(String content,
                                              String password,
                                              String iv) {
        return encryptZeroCBC(content, password, RNG_ALGORITHM, iv, Codec.BASE64);
    }

    /**
     * 基于{@link #ZERO_CBC_ALGORITHM}算法对{@link Codec#BASE64}编码后的密文{@code content}进行解密
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  {@link Codec#BASE64}编码后的密文
     * @param password 密码
     * @param iv       iv向量参数
     * @return 明文
     */
    public static String decryptZeroCBCBase64(String content,
                                              String password,
                                              String iv) {
        return decryptZeroCBC(content, password, RNG_ALGORITHM, iv, Codec.BASE64);
    }

    /**
     * 基于{@link #ZERO_CBC_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String encryptZeroCBCHex(String content,
                                           String password,
                                           String iv) {
        return encryptZeroCBC(content, password, RNG_ALGORITHM, iv, Codec.HEX);
    }

    /**
     * 基于{@link #ZERO_CBC_ALGORITHM}算法对{@link Codec#HEX}编码后的密文{@code content}进行解密
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  {@link Codec#HEX}编码后的密文
     * @param password 密码
     * @param iv       iv向量参数
     * @return 明文
     */
    public static String decryptZeroCBCHex(String content,
                                           String password,
                                           String iv) {
        return decryptZeroCBC(content, password, RNG_ALGORITHM, iv, Codec.HEX);
    }

    //------------------------------------------------------------------------------------------------------------------zero GCM

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#BASE64}对密文进行编码
     *
     * @param content  明文
     * @param password 密码
     * @return {@link Codec#BASE64}编码后的密文
     */
    public static String weakEncryptZeroGCMBase64(String content,
                                                  String password) {
        return encryptZeroGCM(content, password, null, null, Codec.BASE64);
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#BASE64}对密文进行编码
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#BASE64}编码后的密文
     */
    public static String weakEncryptZeroGCMBase64(String content,
                                                  String password,
                                                  String iv) {
        return encryptZeroGCM(content, password, null, iv, Codec.BASE64);
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{@link Codec#BASE64}编码后的密文{@code content}进行解密
     *
     * @param content  {@link Codec#BASE64}编码后的密文
     * @param password 密码
     * @return 明文
     */
    public static String weakDecryptZeroGCMBase64(String content,
                                                  String password) {
        return decryptZeroGCM(content, password, null, Codec.BASE64);
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     *
     * @param content  明文
     * @param password 密码
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String weakEncryptZeroGCMHex(String content,
                                               String password) {
        return encryptZeroGCM(content, password, null, null, Codec.HEX);
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String weakEncryptZeroGCMHex(String content,
                                               String password,
                                               String iv) {
        return encryptZeroGCM(content, password, null, iv, Codec.HEX);
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{@link Codec#HEX}编码后的密文{@code content}进行解密
     *
     * @param content  {@link Codec#HEX}编码后的密文
     * @param password 密码
     * @return 明文
     */
    public static String weakDecryptZeroGCMHex(String content,
                                               String password) {
        return decryptZeroGCM(content, password, null, Codec.HEX);
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#BASE64}对密文进行编码
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  明文
     * @param password 密码
     * @return {@link Codec#BASE64}编码后的密文
     */
    public static String encryptZeroGCMBase64(String content,
                                              String password) {
        return encryptZeroGCM(content, password, RNG_ALGORITHM, null, Codec.BASE64);
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#BASE64}对密文进行编码
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#BASE64}编码后的密文
     */
    public static String encryptZeroGCMBase64(String content,
                                              String password,
                                              String iv) {
        return encryptZeroGCM(content, password, RNG_ALGORITHM, iv, Codec.BASE64);
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{@link Codec#BASE64}编码后的密文{@code content}进行解密
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  {@link Codec#BASE64}编码后的密文
     * @param password 密码
     * @return 明文
     */
    public static String decryptZeroGCMBase64(String content,
                                              String password) {
        return decryptZeroGCM(content, password, RNG_ALGORITHM, Codec.BASE64);
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  明文
     * @param password 密码
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String encryptZeroGCMHex(String content,
                                           String password) {
        return encryptZeroGCM(content, password, RNG_ALGORITHM, null, Codec.HEX);
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{}进行明文{@code content}进行加密, 然后使用{@link Codec#HEX}对密文进行编码
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  明文
     * @param password 密码
     * @param iv       iv向量参数
     * @return {@link Codec#HEX}编码后的密文
     */
    public static String encryptZeroGCMHex(String content,
                                           String password,
                                           String iv) {
        return encryptZeroGCM(content, password, RNG_ALGORITHM, iv, Codec.HEX);
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{@link Codec#HEX}编码后的密文{@code content}进行解密
     * 使用{@link #RNG_ALGORITHM}算法提高秘钥安全性
     *
     * @param content  {@link Codec#HEX}编码后的密文
     * @param password 密码
     * @return 明文
     */
    public static String decryptZeroGCMHex(String content,
                                           String password) {
        return decryptZeroGCM(content, password, RNG_ALGORITHM, Codec.HEX);
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 基于{@link #ECB_ALGORITHM}算法对{@code content}加密
     *
     * @param content      明文
     * @param password     密码
     * @param rngAlgorithm 强随机因子算法, 用于提高秘钥安全性
     * @param codec        密文编解码类型
     * @return 编码后的密文
     */
    private static String encryptECB(String content,
                                     String password,
                                     String rngAlgorithm,
                                     Codec codec) {
        try {
            //创建密码器
            Cipher cipher = Cipher.getInstance(ECB_ALGORITHM);
            //初始化为加密模式的密码器
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeyFromCache(password, rngAlgorithm));

            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            //加密
            byte[] result = cipher.doFinal(contentBytes);
            //密文编码
            return codec.encode(result);
        } catch (Exception e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 基于{@link #ECB_ALGORITHM}算法对{@code content}解密
     *
     * @param content      密文
     * @param password     密码
     * @param rngAlgorithm 强随机因子算法, 用于提高秘钥安全性
     * @param codec        密文编解码类型
     * @return 明文
     */
    private static String decryptECB(String content,
                                     String password,
                                     String rngAlgorithm,
                                     Codec codec) {
        try {
            //创建密码器
            Cipher cipher = Cipher.getInstance(ECB_ALGORITHM);
            //初始化为加密模式的密码器
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeyFromCache(password, rngAlgorithm));
            //解密
            byte[] result = cipher.doFinal(codec.decode(content));
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DecryptException(e);
        }
    }

    /**
     * 基于{@link #CBC_ALGORITHM}算法对{@code content}加密
     *
     * @param content      明文
     * @param password     密码
     * @param rngAlgorithm 强随机因子算法, 用于提高秘钥安全性
     * @param iv           iv向量参数
     * @param codec        密文编解码类型
     * @return 编码后的密文
     */
    private static String encryptCBC(String content,
                                     String password,
                                     String rngAlgorithm,
                                     String iv,
                                     Codec codec) {
        try {
            //创建密码器
            Cipher cipher = Cipher.getInstance(CBC_ALGORITHM);
            //初始化为加密模式的密码器
            //使用CBC模式, 需要一个向量iv, 可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeyFromCache(password, rngAlgorithm), getIvParameterSpec(iv));

            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            //加密
            byte[] result = cipher.doFinal(contentBytes);
            //密文编码
            return codec.encode(result);
        } catch (Exception e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 基于{@link #CBC_ALGORITHM}算法对{@code content}解密
     *
     * @param content      密文
     * @param password     密码
     * @param rngAlgorithm 强随机因子算法, 用于提高秘钥安全性
     * @param iv           iv向量参数
     * @param codec        密文编解码类型
     * @return 明文
     */
    private static String decryptCBC(String content,
                                     String password,
                                     String rngAlgorithm,
                                     String iv,
                                     Codec codec) {
        try {
            //创建密码器
            Cipher cipher = Cipher.getInstance(CBC_ALGORITHM);
            //初始化为加密模式的密码器
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeyFromCache(password, rngAlgorithm), getIvParameterSpec(iv));
            //解密
            byte[] result = cipher.doFinal(codec.decode(content));
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DecryptException(e);
        }
    }

    /**
     * 基于{@link #ZERO_CBC_ALGORITHM}算法对{@code content}加密
     *
     * @param content      明文
     * @param password     密码
     * @param rngAlgorithm 强随机因子算法, 用于提高秘钥安全性
     * @param iv           iv向量参数
     * @param codec        密文编解码类型
     * @return 编码后的密文
     */
    private static String encryptZeroCBC(String content,
                                         String password,
                                         String rngAlgorithm,
                                         String iv,
                                         Codec codec) {
        try {
            //创建密码器
            Cipher cipher = Cipher.getInstance(ZERO_CBC_ALGORITHM);
            //初始化为加密模式的密码器
            //使用CBC模式, 需要一个向量iv, 可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeyFromCache(password, rngAlgorithm), getIvParameterSpec(iv));
            //对其位数
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            int blockSize = cipher.getBlockSize();
            int length = contentBytes.length;
            //计算需填充长度
            if (length % blockSize != 0) {
                length = length + (blockSize - (length % blockSize));
            }
            byte[] finalContentBytes = new byte[length];
            System.arraycopy(contentBytes, 0, finalContentBytes, 0, contentBytes.length);
            //加密
            byte[] result = cipher.doFinal(finalContentBytes);
            //密文编码
            return codec.encode(result);
        } catch (Exception e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 基于{@link #ZERO_CBC_ALGORITHM}算法对{@code content}解密
     *
     * @param content      密文
     * @param password     密码
     * @param rngAlgorithm 强随机因子算法, 用于提高秘钥安全性
     * @param iv           iv向量参数
     * @param codec        密文编解码类型
     * @return 明文
     */
    private static String decryptZeroCBC(String content,
                                         String password,
                                         String rngAlgorithm,
                                         String iv,
                                         Codec codec) {
        try {
            //创建密码器
            Cipher cipher = Cipher.getInstance(ZERO_CBC_ALGORITHM);
            //初始化为加密模式的密码器
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeyFromCache(password, rngAlgorithm), getIvParameterSpec(iv));
            //解密
            byte[] result = cipher.doFinal(codec.decode(content));
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DecryptException(e);
        }
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{@code content}加密
     *
     * @param content      明文
     * @param password     密码
     * @param rngAlgorithm 强随机因子算法, 用于提高秘钥安全性
     * @param iv           iv向量参数
     * @param codec        密文编解码类型
     * @return 编码后的密文
     */
    private static String encryptZeroGCM(String content,
                                         String password,
                                         String rngAlgorithm,
                                         String iv,
                                         Codec codec) {
        try {
            //创建密码器
            Cipher cipher = Cipher.getInstance(ZERO_GCM_ALGORITHM);
            //初始化为加密模式的密码器
            //向量iv, 可增加加密算法的强度
            byte[] ivBytes;
            if (StringUtils.isNotBlank(iv)) {
                ivBytes = iv.getBytes(StandardCharsets.UTF_8);
            } else {
                //随机生成向量iv
                ivBytes = randomIv(rngAlgorithm);
            }
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeyFromCache(password, rngAlgorithm), getGCMParameterSpec(ivBytes));
            //加密
            byte[] result = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));

            //组装, iv + tag(密文后16个字节) + 密文(不包含tag, 即不包含后16位个字节)
            int tagByteLen = GCM_TAG_BIT_LEN / 8;
            byte[] ivTagCipherText = new byte[ivBytes.length + result.length];
            System.arraycopy(ivBytes, 0, ivTagCipherText, 0, ivBytes.length);
            System.arraycopy(result, result.length - tagByteLen, ivTagCipherText, ivBytes.length, tagByteLen);
            System.arraycopy(result, 0, ivTagCipherText, ivBytes.length + tagByteLen, result.length - tagByteLen);

            //密文编码
            return codec.encode(ivTagCipherText);
        } catch (Exception e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 基于{@link #ZERO_GCM_ALGORITHM}算法对{@code content}解密
     *
     * @param content      密文
     * @param password     密码
     * @param rngAlgorithm 强随机因子算法, 用于提高秘钥安全性
     * @param codec        密文编解码类型
     * @return 明文
     */
    private static String decryptZeroGCM(String content,
                                         String password,
                                         String rngAlgorithm,
                                         Codec codec) {
        try {
            //密文解码
            byte[] ivTagCipherBytes = codec.decode(content);
            byte[] ivBytes = new byte[IV_LEN];
            System.arraycopy(ivTagCipherBytes, 0, ivBytes, 0, IV_LEN);
            int tagByteLen = GCM_TAG_BIT_LEN / 8;
            int cipherLen = ivTagCipherBytes.length - IV_LEN;
            byte[] cipherBytes = new byte[cipherLen];
            System.arraycopy(ivTagCipherBytes, IV_LEN + tagByteLen, cipherBytes, 0, cipherLen - tagByteLen);
            System.arraycopy(ivTagCipherBytes, IV_LEN, cipherBytes, cipherLen - tagByteLen, tagByteLen);

            //创建密码器
            Cipher cipher = Cipher.getInstance(ZERO_GCM_ALGORITHM);
            //初始化为加密模式的密码器
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeyFromCache(password, rngAlgorithm), getGCMParameterSpec(ivBytes));
            //解密
            byte[] result = cipher.doFinal(cipherBytes);
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DecryptException(e);
        }
    }

    /**
     * 从缓存中获取AES秘钥, 如果没有, 则生成一个新的AES秘钥
     *
     * @param password     密码
     * @param rngAlgorithm 强随机因子算法, 用于提高秘钥安全性
     * @return AES秘钥
     */
    private static SecretKeySpec getSecretKeyFromCache(String password, String rngAlgorithm) throws Exception {
        String key = rngAlgorithm + "-" + password;
        return SECRET_KEY_SPEC_CACHE.get(key, () -> getSecretKey(password, rngAlgorithm));
    }

    /**
     * 生成AES秘钥
     *
     * @param password     密码
     * @param rngAlgorithm 强随机因子算法, 用于提高秘钥安全性
     * @return AES秘钥
     */
    private static SecretKeySpec getSecretKey(String password, String rngAlgorithm) throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(rngAlgorithm)) {
            return new SecretKeySpec(password.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);
        } else {
            // 返回生成指定算法密钥生成器的 KeyGenerator 对象
            KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            SecureRandom random = SecureRandom.getInstance(rngAlgorithm);
            random.setSeed(password.getBytes());
            //AES 要求密钥长度为 128
            kg.init(128, random);
            //生成一个密钥
            SecretKey secretKey = kg.generateKey();
            //转换为AES专用密钥
            return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
        }
    }

    /**
     * 返回向量iv参数
     *
     * @param iv 向量iv
     * @return 向量iv参数
     */
    private static IvParameterSpec getIvParameterSpec(String iv) {
        return new IvParameterSpec(iv.getBytes());
    }

    /**
     * 返回GCM参数
     *
     * @return GCM参数
     */
    private static GCMParameterSpec getGCMParameterSpec(byte[] ivBytes) {
        return new GCMParameterSpec(GCM_TAG_BIT_LEN, ivBytes);
    }

    /**
     * 返回GCM参数
     *
     * @return GCM参数
     */
    private static GCMParameterSpec getGCMParameterSpec(String rngAlgorithm) throws NoSuchAlgorithmException {
        return getGCMParameterSpec(randomIv(rngAlgorithm));
    }

    /**
     * 随机生成iv向量
     *
     * @return iv向量bytes
     */
    private static byte[] randomIv(String rngAlgorithm) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstance(rngAlgorithm);
        byte[] iv = new byte[IV_LEN];
        secureRandom.nextBytes(iv);
        return iv;
    }

    //----------------------------------------------------------------------------------------------------------------------------------------

    /** 密文编解码 */
    private enum Codec {
        BASE64() {
            @Override
            byte[] decode(String s) {
                return Base64.getDecoder().decode(s);
            }

            @Override
            String encode(byte[] bytes) {
                return Base64.getEncoder().encodeToString(bytes);
            }
        },
        HEX() {
            @Override
            byte[] decode(String s) {
                return Hex.decode(s);
            }

            @Override
            String encode(byte[] bytes) {
                return Hex.encode(bytes);
            }
        };

        /**
         * 密文解码
         *
         * @param s 编码后的密文
         * @return 原始密文
         */
        abstract byte[] decode(String s);

        /**
         * 原始密文编码
         *
         * @param bytes 原始密文
         * @return 编码后的密文
         */
        abstract String encode(byte[] bytes);
    }

    private AESUtils() {
    }
}
