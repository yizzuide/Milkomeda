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

import com.github.yizzuide.milkomeda.light.LightContext;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Intercept to check token before request handle.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2022/12/07 00:44
 */
public class CrustInterceptor implements HandlerInterceptor {
    public static final String LIGHT_CONTEXT_ID = "crustLightContext";

    @Resource
    private CrustTokenResolver tokenResolver;

    private LightContext<?, CrustUserInfo<?, ?>> lightContext;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String token = tokenResolver.getRequestToken();
        boolean isAuthedSuccess = false;
        String errorMsg = "Required token is not set.";
        if (StringUtils.hasText(token)) {
            CrustUserInfo<?, ?> userInfo = tokenResolver.resolve(token, null);
            if (userInfo == null) {
                errorMsg = "Token is invalid.";
            } else {
                isAuthedSuccess = true;
                if (lightContext == null) {
                    lightContext = LightContext.setValue(null, LIGHT_CONTEXT_ID);
                }
                lightContext.setData(userInfo);
            }
        }
        if (!isAuthedSuccess) {
            throw new CrustException(errorMsg);
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        if (lightContext != null) {
            lightContext.remove();
        }
    }
}
