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
import com.github.yizzuide.milkomeda.crust.CrustException;
import com.github.yizzuide.milkomeda.crust.CrustPermission;
import com.github.yizzuide.milkomeda.crust.CrustUserInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.util.List;

/**
 * API服务用户信息
 *
 * @since 3.20.0
 * @author yizzuide
 * Create at 2024/01/12 17:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CrustApiUserInfo<T> extends CrustUserInfo<T, CrustPermission> {

    
    private static final long serialVersionUID = 6717030250582125962L;

    /**
     * generated token random.
     */
    private String tokenRand;

    /**
     * guard rules for check request are allowed.
     */
    private List<GuardRule> guardRules;

    public T getEntity() {
        if (this.entity == null) {
            T entity = CrustContext.get().loadEntity(this.uid);
            this.setEntity(entity);
        }
        return this.entity;
    }

    /**
     * Get user details which used to request access filter.
     * @return user details
     */
    public UserDetails getGuardUserDetails() {
        if (CrustContext.get() instanceof CrustApi) {
            CrustApi crustApi = (CrustApi) CrustContext.get();
            return crustApi.loadGuardUserDetails(this.uid);
        }
        throw new CrustException("Crust context get type must be [CrustApi], current type is " + CrustContext.get().getClass().getName());
    }
}
