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

import java.io.Serializable;

/**
 * User details for api service.
 *
 * @since 3.20.0
 * @author yizzuide
 * Create at 2024/01/15 23:19
 */
public class CrustApiUserDetails implements UserDetails {

    private final CrustApiUserInfo<CrustEntity> userInfo;
    
    private static final long serialVersionUID = -4961178605069846241L;

    public CrustApiUserDetails(CrustApiUserInfo<CrustEntity> userInfo) {
        this.userInfo = userInfo;
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
    public CrustApiUserInfo<CrustEntity> userInfo() {
        return this.userInfo;
    }

    @Override
    public boolean accountExpired() {
        CrustEntity entity = userInfo.getEntity();
        if (entity instanceof CrustStatefulEntity) {
            return ((CrustStatefulEntity) entity).accountExpired();
        }
        return false;
    }

    @Override
    public boolean accountLocked() {
        CrustEntity entity = userInfo.getEntity();
        if (entity instanceof CrustStatefulEntity) {
            return ((CrustStatefulEntity) entity).accountLocked();
        }
        return false;
    }

    @Override
    public boolean credentialsExpired() {
        CrustEntity entity = userInfo.getEntity();
        if (entity instanceof CrustStatefulEntity) {
            return ((CrustStatefulEntity) entity).credentialsExpired();
        }
        return false;
    }

    @Override
    public boolean enabled() {
        CrustEntity entity = userInfo.getEntity();
        if (entity instanceof CrustStatefulEntity) {
            return ((CrustStatefulEntity) entity).enabled();
        }
        return true;
    }
}
