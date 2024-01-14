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
import com.github.yizzuide.milkomeda.crust.CrustPermission;
import com.github.yizzuide.milkomeda.crust.CrustUserInfo;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * User service for load user info from repository.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2024/01/12 18:52
 */
public interface CrustApiUserDetailsService {

    /**
     * load user info with an account.
     * @param account   user account
     * @return  CrustUserInfo
     */
    default CrustUserInfo<CrustEntity, CrustPermission> loadUserByUsername(String account) {
        CrustEntity entity = findEntityByUsername(account);
        if (entity == null) {
            throw new CrustUsernameNotFoundException("Not found entity with account: " + account);
        }
        CrustUserInfo<CrustEntity, CrustPermission> userInfo = new CrustApiUserInfo<>();
        userInfo.setUid(entity.getUid());
        userInfo.setUsername(account);
        userInfo.setEntity(entity);
        return userInfo;
    }

    /**
     * Find user entity with username.
     * @param username  user name
     * @return  CrustEntity
     */
    @Nullable
    CrustEntity findEntityByUsername(String username);

    /**
     * Find user entity by uid after authentication.
     * @param uid   user id
     * @return  CrustEntity
     */
    @Nullable
    CrustEntity findEntityById(Serializable uid);

    /**
     * Find login random by uid.
     * @param uid   user id
     * @return  login rand
     */
    String findLoginRand(Serializable uid);
}
