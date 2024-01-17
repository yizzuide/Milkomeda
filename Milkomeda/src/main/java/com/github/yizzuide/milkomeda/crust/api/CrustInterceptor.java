/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.crust.api;

import com.github.yizzuide.milkomeda.crust.CrustContext;
import com.github.yizzuide.milkomeda.crust.CrustEntity;
import com.github.yizzuide.milkomeda.hydrogen.uniform.ResultVO;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformHandler;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformResult;
import com.github.yizzuide.milkomeda.light.LightContext;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPathMatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.util.Objects;

/**
 * This interceptor check secure data before access resource.
 *
 * @since 3.15.0
 * @version 4.0.0
 * @author yizzuide
 * <br>
 * Create at 2022/12/07 00:44
 */
public class CrustInterceptor implements AsyncHandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String token = CrustContext.get().getToken(false);
        if (StringUtils.isBlank(token)) {
            writeFailResponse(response, HttpStatus.UNAUTHORIZED.value(), "Required token is not set.");
            return false;
        }

        SimpleTokenResolver.TokenData tokenData = SimpleTokenResolver.parseToken(token);
        if (tokenData == null) {
            writeFailResponse(response, HttpStatus.UNAUTHORIZED.value(), "Token is invalid.");
            return false;
        }

        CrustApiUserInfo<CrustEntity> userInfo = new CrustApiUserInfo<>();
        userInfo.setUid(tokenData.getUserId());
        UserDetails guardDetails = userInfo.getGuardUserDetails();
        if (guardDetails != null) {
            if (guardDetails.getTokenRand() != null && !Objects.equals(tokenData.getRand(), guardDetails.getTokenRand())) {
                writeFailResponse(response, HttpStatus.UNAUTHORIZED.value(), "Token is invalid.");
                return false;
            }
            if (!guardDetails.enabled() || guardDetails.accountExpired() || guardDetails.accountLocked()) {
                writeFailResponse(response, UniformHandler.REQUEST_USER_ACCESS_FORBIDDEN, "Restricted user access");
                return false;
            }
            // match guard rules
            if (!CollectionUtils.isEmpty(guardDetails.userInfo().getGuardRules())) {
                for (GuardRule guardRule : guardDetails.userInfo().getGuardRules()) {
                    if (URLPathMatcher.match(guardRule.getIncludeUrls(), null)) {
                        if (guardRule.getMatcher().apply(userInfo.getEntity())) {
                            writeFailResponse(response, guardRule.getStatus(), guardRule.getMessage());
                            return false;
                        }
                    }
                }
            }
        }

        userInfo.setToken(token);
        userInfo.setTokenExpire(tokenData.getTimestamp());
        LightContext.setValue(userInfo, CrustApi.LIGHT_CONTEXT_ID);
        return true;
    }

    private void writeFailResponse(@NonNull HttpServletResponse response, int status, @NonNull String errorMsg) throws Exception {
        response.setStatus(status);
        ResultVO<?> source = UniformResult.error(String.valueOf(response.getStatus()), errorMsg);
        UniformHandler.matchStatusToWrite(response, source.toMap());
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        CrustContext.invalidate();
    }
}
