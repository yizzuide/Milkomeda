/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
 * <br>
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
