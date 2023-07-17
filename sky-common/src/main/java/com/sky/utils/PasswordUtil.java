package com.sky.utils;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class PasswordUtil {
    /**
     * 加密（加盐处理）
     * @param password 待加密密码（需要加密的密码）
     * @return 加密后的密码
     */
    public static String encrypt(String password) {
        // 随机盐值 UUID
        String salt = UUID.randomUUID().toString().replaceAll("-", "");
        // 密码=md5(随机盐值+密码)
        String finalPassword = DigestUtils.md5DigestAsHex((salt + password).getBytes());
        return salt + "$" + finalPassword;
    }

    /**
     * 解密
     * @param password       要验证的密码（未加密）
     * @param securePassword 数据库中的加了盐值的密码
     * @return 对比结果 true OR false
     */
    public static boolean decrypt(String password, String securePassword) {
        boolean result = false;
        if (StringUtils.hasLength(password) && StringUtils.hasLength(securePassword)) {
            if (securePassword.length() == 65 && securePassword.contains("$")) {
                String[] securePasswordArr = securePassword.split("\\$");
                // 盐值
                String slat = securePasswordArr[0];
                String finalPassword = securePasswordArr[1];
                // 使用同样的加密算法和随机盐值生成最终加密的密码
                password = DigestUtils.md5DigestAsHex((slat + password).getBytes());
                if (finalPassword.equals(password)) {
                    result = true;
                }
            }
        }
        return result;
    }
}
