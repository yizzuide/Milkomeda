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

import com.github.yizzuide.milkomeda.crust.*;
import com.github.yizzuide.milkomeda.light.LightContext;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;


/**
 * Crust used for api service.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2024/01/12 18:08
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CrustApi extends AbstractCrust {

    public static final String LIGHT_CONTEXT_ID = "CrustApiLightContext";


    @Getter
    @Autowired
    private CrustProperties props;

    @Lazy
    @Autowired
    private CrustApiUserDetailsService crustApiUserDetailsService;


    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends CrustEntity> CrustUserInfo<T, CrustPermission> login(@NotNull String account, @NotNull String credentials, @NotNull Class<T> entityClazz) {
        if (props.getLoginType() != CrustLoginType.CODE) {
            throw new IllegalArgumentException("current login type[" + props.getLoginType().name() + "] not supported");
        }
        // 验证码否正确
        String code = getCode(account);
        if (code == null) {
            throw new CrustBadCredentialsException("verify code is null");
        }
        if (!code.equals(credentials)) {
            throw new CrustBadCredentialsException("verify code not match");
        }
        CrustApiUserInfo<T> userInfo = (CrustApiUserInfo<T>) crustApiUserDetailsService.loadUserByUsername(account);
        long expire = props.getExpire().toMillis();
        String rand = RandomStringUtils.random(5, true, true);
        userInfo.setRand(rand);
        userInfo.setToken(SimpleTokenResolver.createToken(userInfo.getUidLong(), expire, rand));
        userInfo.setTokenExpire(expire);
        return userInfo;
    }

    @Override
    public boolean hasAuthenticated() {
        return getUserInfo() != null;
    }

    @Override
    public <T extends CrustEntity> CrustUserInfo<T, CrustPermission> getUserInfo(Class<T> entityClazz) {
        return LightContext.getValue(LIGHT_CONTEXT_ID);
    }

    @Override
    public CrustUserInfo<?, CrustPermission> refreshToken() {
        CrustUserInfo<CrustEntity, CrustPermission> userInfo = Objects.requireNonNull(LightContext.getValue(LIGHT_CONTEXT_ID));
        long expire = props.getExpire().toMillis();
        String token = SimpleTokenResolver.createToken(userInfo.getUidLong(), expire);
        userInfo.setToken(token);
        userInfo.setTokenExpire(expire);
        return userInfo;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadEntity(Serializable uid) {
        return (T) crustApiUserDetailsService.findEntityById(uid);
    }

    String loadLoginRand(Serializable uid) {
        return crustApiUserDetailsService.findLoginRand(uid);
    }

    @Override
    public void invalidate() {
        LightContext.clearValue(LIGHT_CONTEXT_ID);
    }
}
