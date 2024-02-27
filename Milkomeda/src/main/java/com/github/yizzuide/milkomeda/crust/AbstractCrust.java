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

package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.light.Cache;
import com.github.yizzuide.milkomeda.light.Spot;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.util.StringExtensionsKt;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * Abstract crust can be extended for CMS and api service.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2024/01/12 18:43
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public abstract class AbstractCrust {

    /**
     * 帐号验证码缓存前辍
     */
    private static final String CATCH_CODE_KEY_PREFIX = "crust:code:";

    /**
     * 注册Bean名
     */
    public static final String BEAN_NAME = "crust";

    /**
     * 验证码缓存标识
     */
    public static final String CODE_CATCH_NAME = "crustCodeLightCache";

    @Qualifier(CODE_CATCH_NAME)
    @Autowired(required = false)
    private Cache crustCodeLightCache;

    @Getter
    @Autowired
    protected CrustProperties props;

    /**
     * 获取登录的基本信息，如：uid, username等
     * @return  CrustUserInfo
     * @since 3.15.0
     */
    public CrustUserInfo<?, CrustPermission> getUserInfo() {
        return getUserInfo(null);
    }

    /**
     * Generate verify code for login action.
     * @param account   account name (phone, email, etc.)
     * @param needSend  whether to send SMS code
     * @return verily code
     * @since 3.15.0
     */
    public String generateCode(String account, boolean needSend) {
        String code = props.getTestCode();
        if (needSend) {
            code = RandomStringUtils.randomNumeric(props.getCodeLength());
        }
        String cacheKey = CATCH_CODE_KEY_PREFIX + account;
        // if enableCode is false
        if (crustCodeLightCache == null) {
            return null;
        }
        crustCodeLightCache.set(cacheKey, new Spot<>(code));
        return code;
    }

    /**
     * Get cached code.
     * @param account   account name (phone, email, etc.)
     * @return  verify code
     * @since 3.15.0
     */
    public String getCode(String account) {
        String cacheKey = CATCH_CODE_KEY_PREFIX + account;
        if(crustCodeLightCache == null || !crustCodeLightCache.isCacheL2Exists(cacheKey)) {
            return null;
        }
        Spot<Serializable, String> spot = crustCodeLightCache.get(cacheKey, Serializable.class, String.class);
        return spot == null ? null : spot.getData();
    }

    /**
     * 从请求中获取Token
     * @param checkIsExists 检查是否存在
     * @return Token
     */
    @Nullable
    public String getToken(boolean checkIsExists) {
        String token = WebContext.getRequestNonNull().getHeader(props.getTokenName());
        if (StringExtensionsKt.isEmpty(token)) { return null; }
        // 一般请求头Authorization的值会添加Bearer
        String tokenHead = "Bearer ";
        if (token.contains(tokenHead)) {
            token = token.substring(tokenHead.length());
        }
        return token;
    }

    /**
     * 登录认证
     * @param account 帐号
     * @param credentials 登录凭证（如密码，手机验/邮箱验证码）
     * @param entityClazz 实体类型
     * @param <T> 实体类型
     * @return CrustUserInfo
     */
    @NonNull
    public abstract <T extends CrustEntity> CrustUserInfo<T, CrustPermission> login(@NonNull String account, @NonNull String credentials, @NonNull Class<T> entityClazz);

    /**
     * Check login status is authenticated.
     * @return true is authenticated
     */
    public abstract boolean hasAuthenticated();

    /**
     * 获取当前用户详细信息
     *
     * @param entityClazz 实体类型
     * @param <T> 实体类型
     * @return  CrustUserInfo
     */
    public abstract <T extends CrustEntity> CrustUserInfo<T, CrustPermission> getUserInfo(Class<T> entityClazz);

    /**
     * 刷新令牌
     * @return CrustUserInfo
     */
    public abstract CrustUserInfo<?, CrustPermission> refreshToken();

    /**
     * Custom for permission any match.
     * @param permissions permission list
     * @return  ture if match
     * @since 3.14.0
     */
    public boolean permitAny(String ...permissions) {
        return true;
    }

    /**
     * 从数据源加载用户
     *
     * @param uid user id
     * @return  user entity
     * @param <T> user type
     */
    public abstract <T> T loadEntity(Serializable uid);

    /**
     * 使登录信息失效，清空当前用户的缓存
     */
    public abstract void invalidate();
}
