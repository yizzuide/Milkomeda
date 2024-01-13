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

import com.github.yizzuide.milkomeda.light.Cache;
import com.github.yizzuide.milkomeda.light.CacheHelper;
import com.github.yizzuide.milkomeda.light.LightCacheEvict;
import com.github.yizzuide.milkomeda.light.LightCacheable;
import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.util.StringExtensionsKt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Crust used for CMS service.
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.15.0
 * <br>
 * Create at 2019/11/11 15:48
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Slf4j
public class Crust extends AbstractCrust {

    /**
     * 用户信息缓存前辍
     */
    private static final String CATCH_KEY_PREFIX = "crust:user:";

    /**
     * 缓存标识
     */
    static final String CATCH_NAME = "crustLightCache";

    @Qualifier(Crust.CATCH_NAME)
    @Autowired(required = false)
    private Cache crustLightCache;

    @Autowired
    private CrustTokenResolver tokenResolver;

    private CrustUserDetailsService crustUserDetailsService;

    @NonNull
    public <T extends CrustEntity> CrustUserInfo<T, CrustPermission> login(@NonNull String account, @NonNull String credentials, @NonNull Class<T> entityClazz) {
        return login(account, credentials, entityClazz, props.getLoginType(), null);
    }

    /**
     * 登录认证
     * @param account 帐号
     * @param credentials 登录凭证（如密码，手机验/邮箱验证码）
     * @param entityClazz 实体类型
     * @param loginType 登录类型
     * @param customAuthenticationTokenSupplier Custom authentication token
     * @param <T> 实体类型
     * @return CrustUserInfo
     * @since 3.15.0
     */
    public <T extends CrustEntity> CrustUserInfo<T, CrustPermission> login(@NonNull String account, @NonNull String credentials, @NonNull Class<T> entityClazz, CrustLoginType loginType, Supplier<AbstractAuthenticationToken> customAuthenticationTokenSupplier) {
        AbstractAuthenticationToken authenticationToken = switch (loginType) {
            case PASSWORD -> new CrustAuthenticationToken(account, credentials);
            case CODE -> new CrustCodeAuthenticationToken(account, credentials);
            case CUSTOM -> customAuthenticationTokenSupplier.get();
        };
        // 设置请求信息
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(WebContext.getRequest()));
        AuthenticationManager authenticationManager = ApplicationContextHolder.get().getBean(AuthenticationManager.class);
        // 执行登录认证过程
        // ProviderManager.authenticate(...) -> AbstractUserDetailsAuthenticationProvider.authenticate(...)
        //  -> DaoAuthenticationProvider.retrieveUser(...) -> UserDetailsService.loadUserByUsername(...)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        // 认证成功存储认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // token方式
        if (props.isStateless()) {
            // 生成令牌并返回给客户端
            return generateToken(authentication, entityClazz);
        }
        // session方式
        return getCurrentLoginUserInfo(authentication, entityClazz);
    }

    /**
     * 登录成功后生成令牌
     *
     * @param authentication 认证对象
     * @param entityClazz 实体类型
     * @return CrustUserInfo
     */
    @NonNull
    private <T extends CrustEntity> CrustUserInfo<T, CrustPermission> generateToken(@NonNull Authentication authentication, @NonNull Class<T> entityClazz) {
        CrustUserInfo<T, CrustPermission> userInfo = getCurrentLoginUserInfo(authentication, entityClazz);
        Object principal = authentication.getPrincipal();
        String roleIds = null;
        if (principal instanceof CrustUserDetails) {
            List<Long> roleIdList = ((CrustUserDetails) principal).getRoleIds();
            roleIds = CollectionUtils.isEmpty(roleIdList) ? null : StringUtils.arrayToCommaDelimitedString(roleIdList.toArray());
        }
        String token = tokenResolver.generate(userInfo, roleIds);
        // help cache the auth info，that synchronize with the expired token.
        AopContextHolder.self(this.getClass()).getAuthInfoFromToken(token);
        return userInfo;
    }

    /**
     * 获取登录时用户信息
     * <br>
     * Session方式 和 Token方式登录/刷新Token 时都会调用（Session方式获取用户信息也走这里）
     *
     * @param authentication    认证信息
     * @param clazz             实体类型
     * @param <T>               实体类型
     * @return  CrustUserInfo
     */
    @SuppressWarnings("unchecked")
    private <T extends CrustEntity> CrustUserInfo<T, CrustPermission> getCurrentLoginUserInfo(Authentication authentication, Class<T> clazz) {
        if (authentication == null) {
            throw new AuthenticationServiceException("Authentication is must be not null");
        }
        // 此时 authentication 就是内部封装的 UsernamePasswordAuthenticationToken
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CrustUserDetails userDetails)) {
            throw new AuthenticationServiceException("Authentication principal must be subtype of CrustUserDetails");
        }
        return (CrustUserInfo<T, CrustPermission>) userDetails.getUserInfo();
    }

    @NonNull
    public <T extends CrustEntity> CrustUserInfo<T, CrustPermission> getUserInfo(Class<T> entityClazz) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            throw new CrustException("authentication is null");
        }

        // Token方式 || Session方式无超级缓存
        if (props.isStateless() || !getProps().isEnableCache()) {
            CrustUserInfo<T, CrustPermission> userInfo = getCurrentLoginUserInfo(authentication, entityClazz);
            if (props.isEnableCache() && entityClazz != null) {
                // set entity class for create real type's object later.
                userInfo.setEntityClass(entityClazz);
            }
            return userInfo;
        }

        // Session方式开启超级缓存
        return CacheHelper.getFastLevel(crustLightCache, spot -> getCurrentLoginUserInfo(authentication, entityClazz));
    }

    /**
     * 根据请求令牌获取登录认证信息
     * @param token 请求令牌
     * @return CrustUserInfo
     */
    @LightCacheable(value = CATCH_NAME, keyPrefix = CATCH_KEY_PREFIX, key = "T(org.springframework.util.DigestUtils).md5DigestAsHex(#token?.bytes)", condition = "#target.props.enableCache")
    // 不使用泛型，因为不知道具体类型，无法恢复回来，让其先转为Map或List
    @SuppressWarnings("rawtypes")
    CrustUserInfo getAuthInfoFromToken(String token) {
        // invoke from generateToken()
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            return getCurrentLoginUserInfo(authentication, null);
        }
        // invoke from CrustAuthenticationFilter.doFilterInternal()
        CrustUserInfo userInfo = tokenResolver.resolve(token, this::loadEntity);
        if (userInfo == null) {
            return null;
        }
        // active!
        activeAuthentication(userInfo);
        return userInfo;
    }

    // #token 与 args[0] 等价
    @LightCacheEvict(value = Crust.CATCH_NAME, keyPrefix = CATCH_KEY_PREFIX, key = "T(org.springframework.util.DigestUtils).md5DigestAsHex(#token?.bytes)", condition = "#target.props.enableCache")
    void removeTokenCache(String token) {
    }

    /**
     * Active authentication with crust user info.
     * @param userInfo CrustUserInfo
     * @since 3.14.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    void activeAuthentication(CrustUserInfo userInfo) {
        // has find perms from cache?
        List permissionList = userInfo.getPermissionList();
        CrustPerm crustPerm = permissionList == null ? getCrustUserDetailsService().findPermissionsById(userInfo.getUid()) :
                CrustPerm.builder().permissionList(permissionList).build();
        List<GrantedAuthority> authorities = null;
        if (crustPerm != null) {
            authorities = CrustPerm.buildAuthorities(crustPerm.getPermissionList());
            if (permissionList == null) {
                userInfo.setPermissionList(crustPerm.getPermissionList());
            }
        }
        CrustUserDetails userDetails = new CrustUserDetails(userInfo.getUid(), userInfo.getUsername(), authorities, userInfo.getRoleIds());
        userDetails.setUserInfo(userInfo);
        Authentication authentication = new CrustAuthenticationToken(userDetails, null, authorities, userInfo.getToken());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public CrustUserInfo<?, CrustPermission> refreshToken() {
        if (!props.isStateless()) { return null; }
        String refreshedToken;
        try {
            CrustUserInfo<?, CrustPermission> loginUserInfo = getCurrentLoginUserInfo(getAuthentication(), null);
            String token = getToken(false);
            refreshedToken = tokenResolver.refresh(token, loginUserInfo);
            // remove cache (old token with user auth info)
            AopContextHolder.self(this.getClass()).removeTokenCache(token);
            // re-cache user auth info with refreshed token, not need to change it.
            AopContextHolder.self(this.getClass()).getAuthInfoFromToken(refreshedToken);
            return loginUserInfo;
        } catch (Exception e) {
            log.warn("Crust refresh token fail with msg: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 使登录信息失效
     */
    public void invalidate() {
        // Token方式下开启缓存时清空
        if (getProps().isStateless()) {
            String token = getAuthentication() == null ? getToken(false) :
                    getUserInfo(null).getToken();
            if (StringExtensionsKt.isEmpty(token)) {
                throw new InsufficientAuthenticationException("Token is not exists.");
            }
            AopContextHolder.self(this.getClass()).removeTokenCache(token);
        }
    }

    /**
     * 获取当前登录信息
     *
     * @return Authentication
     */
    private Authentication getAuthentication() {
        if (SecurityContextHolder.getContext() == null) {
            return null;
        }
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取Spring Security上下文
     * @return SecurityContext
     */
    @NonNull
    public SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }


    @Nullable
    public String getToken(boolean checkIsExists) {
        if (!props.isStateless()) { return null; }
        String token = super.getToken(checkIsExists);
        if (token == null || props.isCacheInMemory()) {
            return token;
        }
        if (checkIsExists && props.isEnableCache()) {
            String cacheKey = CATCH_KEY_PREFIX + DigestUtils.md5DigestAsHex(token.getBytes());
            if (!crustLightCache.isCacheL2Exists(cacheKey)) {
                return null;
            }
        }
        return token;
    }

    /**
     * Custom for permission any match.
     * @param permissions permission list
     * @return  ture if match
     * @since 3.14.0
     */
    public boolean permitAny(String ...permissions) {
        Authentication authentication = getAuthentication();
        Assert.notNull(authentication, "Authentication must be not null.");
        CrustUserInfo<CrustEntity, CrustPermission> loginUserInfo = getCurrentLoginUserInfo(authentication, null);
        Boolean isAdmin = loginUserInfo.getIsAdmin();
        if (isAdmin != null && isAdmin) {
            return true;
        }
        List<String> perms = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return Arrays.stream(permissions).anyMatch(perms::contains);
    }

    @SuppressWarnings("unchecked")
    public <T> T loadEntity(Serializable uid) {
        return (T) getCrustUserDetailsService().findEntityById(uid);
    }

    private CrustUserDetailsService getCrustUserDetailsService() {
        if (crustUserDetailsService == null) {
            crustUserDetailsService = ApplicationContextHolder.get().getBean(CrustUserDetailsService.class);
        }
        return crustUserDetailsService;
    }
}
