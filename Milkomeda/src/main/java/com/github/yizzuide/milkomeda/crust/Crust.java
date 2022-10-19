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
import com.github.yizzuide.milkomeda.util.JwtUtil;
import com.github.yizzuide.milkomeda.util.Strings;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Crust
 * 核心API
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.12.10
 * <br>
 * Create at 2019/11/11 15:48
 */
@Slf4j
public class Crust {
    /**
     * 缓存前辍
     */
    private static final String CATCH_KEY_PREFIX = "crust:user:";
    /**
     * 缓存标识
     */
    static final String CATCH_NAME = "lightCacheCrust";
    /**
     * 用户id
     */
    private static final String UID = "uid";
    /**
     * 用户名称
     */
    private static final String USERNAME = Claims.SUBJECT;
    /**
     * 创建时间
     */
    private static final String CREATED = Claims.ISSUED_AT;
    /**
     * 角色id
     */
    private static final String ROLE_IDS = "roles";

    @Getter
    @Autowired
    private CrustProperties props;

    @Qualifier(Crust.CATCH_NAME)
    @Autowired(required = false)
    private Cache lightCacheCrust;

    private CrustUserDetailsService crustUserDetailsService;

    /**
     * 登录认证
     *
     * @param username 用户名
     * @param password 密码
     * @param entityClazz 实体类型
     * @param <T> 实体类型
     * @return CrustUserInfo
     */
    @NonNull
    public <T extends CrustEntity> CrustUserInfo<T, CrustPermission> login(@NonNull String username, @NonNull String password, @NonNull Class<T> entityClazz) {
        // CrustAuthenticationToken封装了UsernamePasswordAuthenticationToken
        CrustAuthenticationToken authenticationToken = new CrustAuthenticationToken(username, password);
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
        Map<String, Object> claims = new HashMap<>(6);
        CrustUserInfo<T, CrustPermission> userInfo = getCurrentLoginUserInfo(authentication, entityClazz);
        claims.put(UID, userInfo.getUidLong());
        claims.put(USERNAME, userInfo.getUsername());
        claims.put(CREATED, new Date());
        Object principal = authentication.getPrincipal();
        if (principal instanceof CrustUserDetails) {
            List<Long> roleIds = ((CrustUserDetails) principal).getRoleIds();
            if (!CollectionUtils.isEmpty(roleIds)) {
                claims.put(ROLE_IDS, StringUtils.arrayToCommaDelimitedString(roleIds.toArray()));
            }
        }
        long expire = LocalDateTime.now().plusMinutes(props.getExpire().toMinutes()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String token = JwtUtil.generateToken(claims, getSignKey(), expire, props.isUseRsa());
        userInfo.setToken(token);
        userInfo.setTokenExpire(expire);
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
        if (!(principal instanceof CrustUserDetails)) {
            throw new AuthenticationServiceException("Authentication principal must be subtype of CrustUserDetails");
        }
        CrustUserDetails userDetails = (CrustUserDetails) principal;
        return (CrustUserInfo<T, CrustPermission>) userDetails.getUserInfo();
    }

    /**
     * 获取当前用户信息（外面API调用）
     *
     * @param entityClazz 实体类型
     * @param <T> 实体类型
     * @return  CrustUserInfo
     */
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
        return CacheHelper.getFastLevel(lightCacheCrust, spot -> getCurrentLoginUserInfo(authentication, entityClazz));
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
        String unSignKey = getUnSignKey();
        Claims claims = JwtUtil.parseToken(token, unSignKey);
        if (claims == null) {
            return null;
        }
        String username = claims.getSubject();
        if (username == null) {
            return null;
        }
        Serializable uid = (Serializable) claims.get(UID);
        //long issuedAt = (long) claims.get(CREATED);
        long expire = claims.getExpiration().getTime();
        Object RoleIdsObj = claims.get(ROLE_IDS);
        List<Long> roleIds = null;
        if (RoleIdsObj != null) {
            roleIds = Arrays.stream(((String) RoleIdsObj).split(",")).map(Long::parseLong).collect(Collectors.toList());
        }
        CrustEntity entity = getCrustUserDetailsService().findEntityById(uid);
        CrustUserInfo userInfo = new CrustUserInfo<>(uid, username, token, roleIds, entity);
        userInfo.setTokenExpire(expire);
        // active!
        activeAuthentication(userInfo);
        return userInfo;
    }

    // #token 与 args[0] 等价
    // #target 与 @crust、#this.object、#root.object等价（#this在表达式不同部分解析过程中可能会改变，但是#root总是指向根，object为自定义root对象属性）
    @LightCacheEvict(value = Crust.CATCH_NAME, keyPrefix = CATCH_KEY_PREFIX, key = "T(org.springframework.util.DigestUtils).md5DigestAsHex(#token?.bytes)", condition = "#target.props.enableCache")
    public void removeTokenCache(String token) {
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

    /**
     * 刷新令牌
     * @return CrustUserInfo
     */
    public CrustUserInfo<?, CrustPermission> refreshToken() {
        if (!props.isStateless()) { return null; }
        String refreshedToken;
        try {
            String token = getToken(false);
            Claims claims = JwtUtil.parseToken(token, getUnSignKey());
            claims.put(CREATED, new Date());
            long expire = LocalDateTime.now().plusMinutes(props.getExpire().toMinutes()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            refreshedToken = JwtUtil.generateToken(claims, getSignKey(), expire, props.isUseRsa());
            CrustUserInfo<?, CrustPermission> loginUserInfo = getCurrentLoginUserInfo(getAuthentication(), null);
            loginUserInfo.setToken(refreshedToken);
            loginUserInfo.setTokenExpire(expire);
            // remove cache (old token with user auth info)
            AopContextHolder.self(this.getClass()).removeTokenCache(token);
            // re-cache user auth info with refreshed token, not need change it.
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
        try {
            // Token方式下开启缓存时清空
            if (getProps().isStateless()) {
                AopContextHolder.self(this.getClass()).removeTokenCache(getUserInfo(null).getToken());
            }
        } finally {
            SecurityContextHolder.clearContext();
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

    /**
     * 从请求中获取Token
     * @param checkIsExists 检查是否存在
     * @return Token
     */
    @Nullable
    public String getToken(boolean checkIsExists) {
        if (!props.isStateless()) { return null; }
        String token = WebContext.getRequestNonNull().getHeader(props.getTokenName());
        if (Strings.isEmpty(token)) { return null; }
        // 一般请求头Authorization的值会添加Bearer
        String tokenHead = "Bearer ";
        if (token.contains(tokenHead)) {
            token = token.substring(tokenHead.length());
        }
        if (checkIsExists && props.isEnableCache()) {
            String cacheKey = CATCH_KEY_PREFIX + DigestUtils.md5DigestAsHex(token.getBytes());
            if (!lightCacheCrust.isCacheL2Exists(cacheKey)) {
                return null;
            }
        }
        return token;
    }

    /**
     * Custom for permission any match.
     * @param permissions permission list.
     * @return  ture if match
     * @since 3.14.0
     */
    public boolean permitAny(String ...permissions) {
        Authentication authentication = getAuthentication();
        Assert.notNull(authentication, "Authentication must be not null.");
        CrustUserInfo<CrustEntity, CrustPermission> loginUserInfo = getCurrentLoginUserInfo(authentication, null);
        List<String> perms = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return loginUserInfo.getIsAdmin() || Arrays.stream(permissions).anyMatch(perms::contains);
    }

    /**
     * 获取加密key
     * @return sign key
     */
    private String getSignKey() {
        if (props.isUseRsa()) {
            return props.getPriKey();
        }
        return props.getSecureKey();
    }

    /**
     * 获取解密key
     * @return unSign key
     */
    private String getUnSignKey() {
        if (props.isUseRsa()) {
            return props.getPubKey();
        }
        return props.getSecureKey();
    }

    /**
     * 从IoC容器查找UserDetailsService
     * @return CrustUserDetailsService
     */
    private CrustUserDetailsService getCrustUserDetailsService() {
        if (crustUserDetailsService == null) {
            crustUserDetailsService = ApplicationContextHolder.get().getBean(CrustUserDetailsService.class);
        }
        return crustUserDetailsService;
    }
}
