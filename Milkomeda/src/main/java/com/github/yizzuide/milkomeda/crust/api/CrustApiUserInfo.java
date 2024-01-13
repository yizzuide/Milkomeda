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
import com.github.yizzuide.milkomeda.crust.CrustPermission;
import com.github.yizzuide.milkomeda.crust.CrustUserInfo;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * API服务用户Info
 *
 * @author yizzuide
 * Create at 2024/01/12 17:51
 */
@NoArgsConstructor
public class CrustApiUserInfo<T> extends CrustUserInfo<T, CrustPermission> {

    @Serial
    private static final long serialVersionUID = 6717030250582125962L;

    private CrustApiUserInfo(Serializable uid, String username, String token, Long tokenExpire) {
        this.uid = uid;
        this.username = username;
        this.token = token;
        this.tokenExpire = tokenExpire;
    }

    public T getEntity() {
        if (this.entity == null) {
            T entity = CrustContext.get().loadEntity(this.uid);
            this.setEntity(entity);
        }
        return this.entity;
    }
}
