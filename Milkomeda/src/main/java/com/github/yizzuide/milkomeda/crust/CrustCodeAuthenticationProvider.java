/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * A Provider supported for code type login with {@link CrustCodeAuthenticationToken}.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2022/12/31 20:57
 */
public class CrustCodeAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;

    public CrustCodeAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CrustCodeAuthenticationToken authenticationToken = (CrustCodeAuthenticationToken) authentication;
        String account = (String) authentication.getPrincipal();
        String code = (String) authentication.getCredentials();
        String cachedCode = CrustContext.getDefault().getCode(account);
        if (cachedCode == null) {
            throw new BadCredentialsException("Verify code not exists!");
        }
        if (!cachedCode.equals(code)) {
            throw new BadCredentialsException("Verify code is invalid!");
        }
        CrustUserDetails loginUser = (CrustUserDetails) this.userDetailsService.loadUserByUsername(account);
        CrustCodeAuthenticationToken authenticationResult = new CrustCodeAuthenticationToken(loginUser, code, loginUser.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());
        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CrustCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
