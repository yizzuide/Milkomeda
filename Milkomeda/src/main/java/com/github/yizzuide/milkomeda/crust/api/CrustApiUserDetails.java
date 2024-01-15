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

import com.github.yizzuide.milkomeda.crust.CrustEntity;
import com.github.yizzuide.milkomeda.crust.CrustStatefulEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Supplier;

/**
 * User details for api service.
 *
 * @param userInfo 用户信息
 * @since 4.0.0
 * @author yizzuide
 * Create at 2024/01/15 23:19
 */
public record CrustApiUserDetails(CrustApiUserInfo<CrustEntity> userInfo) implements UserDetails {

    @Serial
    private static final long serialVersionUID = -4961178605069846241L;

    public static CrustApiUserDetails from(CrustEntity entity, Supplier<String> tokenRandProvider) {
        CrustApiUserInfo<CrustEntity> userInfo = new CrustApiUserInfo<>();
        userInfo.setUid(entity.getUid());
        userInfo.setUsername(entity.getUsername());
        userInfo.setTokenRand(tokenRandProvider.get());
        userInfo.setEntity(entity);
        return new CrustApiUserDetails(userInfo);
    }

    @Override
    public Serializable getUid() {
        return this.userInfo.getUid();
    }

    @Override
    public String getUsername() {
        return this.userInfo.getUsername();
    }

    @Override
    public String getPassword() {
        return this.userInfo.getEntity().getPassword();
    }

    @Override
    public String getTokenRand() {
        return this.userInfo.getTokenRand();
    }

    @Override
    public boolean accountExpired() {
        if (userInfo.getEntity() instanceof CrustStatefulEntity entity) {
            return entity.accountExpired();
        }
        return false;
    }

    @Override
    public boolean accountLocked() {
        if (userInfo.getEntity() instanceof CrustStatefulEntity entity) {
            return entity.accountLocked();
        }
        return false;
    }

    @Override
    public boolean credentialsExpired() {
        if (userInfo.getEntity() instanceof CrustStatefulEntity entity) {
            return entity.credentialsExpired();
        }
        return false;
    }

    @Override
    public boolean enabled() {
        if (userInfo.getEntity() instanceof CrustStatefulEntity entity) {
            return entity.enabled();
        }
        return true;
    }
}
