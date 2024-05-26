package org.kin.framework.utils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author huangjianqin
 * @date 2024/5/21
 */
public abstract class AbstractMessageDigest {
    protected MessageDigest digest;

    public AbstractMessageDigest(String algorithm) {
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    /**
     * 计算字符串哈希值, 返回byte数组
     */
    public byte[] digest(String s) {
        update(s.getBytes(UTF_8));
        return digest();
    }

    /**
     * 计算字符串哈希值, 返回十六进制字符串
     */
    public String digestAsHex(String s) {
        return SecyCodec.HEX.encode(digest(s));
    }

    /**
     * 计算字符串哈希值, 返回Base64字符串
     */
    public String digestAsBase64(String s) {
        return SecyCodec.BASE64.encode(digest(s));
    }

    /**
     * 计算字节流的哈希值, 返回byte数组
     */
    public byte[] digest(InputStream is) {
        byte[] buffer = new byte[4096];
        int num;
        try {
            while ((num = is.read(buffer)) > 0) {
                digest.update(buffer, 0, num);
            }
        } catch (Exception e) {
            ExceptionUtils.throwExt(e);
        }

        return digest();
    }

    /**
     * 计算字节流的哈希值, 返回十六进制字符串
     */
    public String digestAsHex(InputStream is) {
        return SecyCodec.HEX.encode(digest(is));
    }

    /**
     * 计算字节流的哈希值, 返回Base64字符串
     */
    public String digestAsBase64(InputStream is) {
        return SecyCodec.BASE64.encode(digest(is));
    }

    /**
     * 增量计算哈希值
     * 配合{@link #digest()}和{@link #digestAsHex()}一起使用
     * 适合用于计算大字符串的哈希值, 防止OOM
     */
    public void update(String s) {
        digest.update(s.getBytes(UTF_8));
    }

    /**
     * 增量计算哈希值
     * 配合{@link #digest()}和{@link #digestAsHex()}一起使用
     * 适合用于计算大字符串的哈希值, 防止OOM
     */
    public void update(byte[] bytes) {
        digest.update(bytes);
    }

    /**
     * 增量计算哈希值
     * 配合{@link #digest()}和{@link #digestAsHex()}一起使用
     * 适合用于计算大字符串的哈希值, 防止OOM
     */
    public void update(ByteBuffer byteBuffer) {
        digest.update(byteBuffer);
    }

    /**
     * 返回哈希值字节数组
     */
    public byte[] digest() {
        return digest.digest();
    }

    /**
     * 返回哈希值 十六进制字符串
     */
    public String digestAsHex() {
        return SecyCodec.HEX.encode(digest());
    }

    /**
     * 返回哈希值 Base64字符串
     */
    public String digestAsBase64() {
        return SecyCodec.BASE64.encode(digest());
    }
}
