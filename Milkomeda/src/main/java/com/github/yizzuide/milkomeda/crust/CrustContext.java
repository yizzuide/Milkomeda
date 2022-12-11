/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
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
import org.springframework.lang.Nullable;

/**
 * CrustContext
 * Crust上下文，使用这个类直接访问内部提供的API
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.15.0
 * <br>
 * Create at 2019/11/11 17:53
 */
public class CrustContext {

    private static Crust INSTANCE;

    private CrustContext() {}

    static void set(Crust crust) {
        INSTANCE = crust;
    }

    public static Crust get() {
        return INSTANCE;
    }

    /**
     * 获取登录的基本信息，如：uid, username, roleId等
     * @return  CrustUserInfo
     * @since 3.15.0
     */
    public static CrustUserInfo<?, ?> getUserInfo() {
        return getUserInfo(null);
    }

    /**
     * 获取用户信息（支持多次调用，而不会重新创建多份对象）
     *
     * @param entityClass   用户实体类型
     * @param <T>   实体类型
     * @return  CrustUserInfo
     */
    @NonNull
    public static <T extends CrustEntity> CrustUserInfo<T, CrustPermission> getUserInfo(@Nullable Class<T> entityClass) {
        Crust crust = get();
        // invoke from microservice
        if (crust == null) {
            return LightContext.getValue(CrustInterceptor.LIGHT_CONTEXT_ID);
        }
        return crust.getUserInfo(entityClass);
    }

    /**
     * 使登录信息失效，清空当前用户的缓存
     */
    public static void invalidate() {
        Crust crust = get();
        if (crust != null) {
            crust.invalidate();
        }
    }
}
