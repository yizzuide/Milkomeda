package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * PasswordEncoder
 * 自定义加salt密码加密器
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 18:10
 */
public class PasswordEncoder implements org.springframework.security.crypto.password.PasswordEncoder {
    private final static String MD5 = "MD5";
    private final static String SHA = "SHA";

    private final Object salt;
    private final String algorithm;

    public PasswordEncoder(Object salt) {
        this(salt, MD5);
    }

    public PasswordEncoder(Object salt, String algorithm) {
        this.salt = salt;
        this.algorithm = algorithm;
    }

    private String mergePasswordAndSalt(CharSequence password) {
        if (password == null) {
            password = "";
        }
        if ((salt == null) || "".equals(salt)) {
            return password.toString();
        } else {
            return password + "{" + salt + "}";
        }
    }

    @Override
    public String encode(CharSequence rawPassword) {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            // 加密后的字符串
            result = DataTypeConvertUtil.byte2HexStr(md.digest(mergePasswordAndSalt(rawPassword).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ignored) {
        }
        return result;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.length() == 0) {
            return false;
        }
        return encodedPassword.equals(encode(rawPassword));
    }
}
