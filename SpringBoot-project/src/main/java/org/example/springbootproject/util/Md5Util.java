package org.example.springbootproject.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密工具类
 * 用于对密码进行MD5加密
 */
public class Md5Util {
    /**
     * MD5加密
     * @param plainText 明文
     * @return MD5加密后的字符串（32位小写）
     */
    public static String md5(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(plainText.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                int i = b & 0xff;
                if (i < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(i));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}

