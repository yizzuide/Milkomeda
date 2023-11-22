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

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * CrustAuthenticationProvider
 * 认证提供者
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.16.4
 * <br>
 * Create at 2019/11/11 18:02
 * @see org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
 */
public class CrustAuthenticationProvider extends DaoAuthenticationProvider {

    private final CrustProperties props;

    public CrustAuthenticationProvider(CrustProperties props, BCryptPasswordEncoder passwordEncoder) {
        this.props = props;
        // 设置BCrypt加密器
        if (props.isUseBcrypt() && passwordEncoder != null) {
            setPasswordEncoder(passwordEncoder);
        }
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {

        // 如果使用BCrypt密码方式，使用父类默认实现
        if (props.isUseBcrypt()) {
            super.additionalAuthenticationChecks(userDetails, authentication);
            return;
        }

        // 检查登录密码
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        boolean isMatched;
        String presentedPassword = authentication.getCredentials().toString();
        // 如果用户有实现自定义加密器
        if (getPasswordEncoder() != null) {
            isMatched = getPasswordEncoder().matches(presentedPassword, userDetails.getPassword());
        } else {
            // 否则使用内置加密器（数据表密码列+盐列）
            String salt = ((CrustUserDetails) userDetails).getSalt();
            isMatched = new PasswordEncoder(salt).matches(presentedPassword, userDetails.getPassword());
        }

        // 如果验证失败
        if (!isMatched) {
            logger.warn("Authentication failed: password does not match stored value");
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CrustAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
