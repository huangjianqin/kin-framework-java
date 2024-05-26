package org.kin.framework.utils;

import java.util.Base64;

/**
 * Secy即Security
 *
 * @author huangjianqin
 * @date 2024/5/24
 */
enum SecyCodec {
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
            return HexUtils.decode(s);
        }

        @Override
        String encode(byte[] bytes) {
            return HexUtils.encode(bytes);
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