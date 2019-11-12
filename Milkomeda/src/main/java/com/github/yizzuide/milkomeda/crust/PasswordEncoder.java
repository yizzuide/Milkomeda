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
public class PasswordEncoder {
    private final static String MD5 = "MD5";
    private final static String SHA = "SHA";

    private Object salt;
    private String algorithm;

    public PasswordEncoder(Object salt) {
        this(salt, MD5);
    }

    public PasswordEncoder(Object salt, String algorithm) {
        this.salt = salt;
        this.algorithm = algorithm;
    }

    /**
     * 密码加密
     * @param rawPass   源密码
     * @return  加密结果
     */
    public String encode(String rawPass) {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            // 加密后的字符串
            result = DataTypeConvertUtil.byte2HexStr(md.digest(mergePasswordAndSalt(rawPass).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ignored) {
        }
        return result;
    }

    /**
     * 密码匹配验证
     * @param rawPass 明文
     * @param encPass 密文
     * @return 是否匹配
     */
    public boolean matches(String rawPass, String encPass) {
        String pass1 = "" + encPass;
        String pass2 = encode(rawPass);
        return pass1.equals(pass2);
    }

    private String mergePasswordAndSalt(String password) {
        if (password == null) {
            password = "";
        }
        if ((salt == null) || "".equals(salt)) {
            return password;
        } else {
            return password + "{" + salt.toString() + "}";
        }
    }
}
