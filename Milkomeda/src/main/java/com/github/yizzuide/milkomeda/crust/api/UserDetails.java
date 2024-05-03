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

import java.util.function.Supplier;

/**
 * The user details describe user login information which needs to limit request to access resource.
 *
 * @since 3.20.0
 * @author yizzuide
 * Create at 2024/01/15 15:24
 */
public interface UserDetails extends CrustStatefulEntity {

    /**
     * Create user details from entity.
     * @param entity     entity
     * @param tokenRandProvider token random supplier
     * @return user details
     */
    static UserDetails from(CrustEntity entity, Supplier<String> tokenRandProvider) {
        CrustApiUserInfo<CrustEntity> userInfo = new CrustApiUserInfo<>();
        userInfo.setUid(entity.getUid());
        userInfo.setUsername(entity.getUsername());
        userInfo.setTokenRand(tokenRandProvider.get());
        userInfo.setEntity(entity);
        return new CrustApiUserDetails(userInfo);
    }

    /**
     * Get login user info.
     * @return user info
     */
    CrustApiUserInfo<CrustEntity> userInfo();

    /**
     * Created token random string after login.
     * @return  token random string
     */
    String getTokenRand();
}
