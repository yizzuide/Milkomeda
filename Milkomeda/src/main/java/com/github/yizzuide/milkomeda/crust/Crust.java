package com.github.yizzuide.milkomeda.crust;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.light.Cache;
import com.github.yizzuide.milkomeda.light.CacheHelper;
import com.github.yizzuide.milkomeda.light.Spot;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Crust
 * 核心API
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.17.1
 * Create at 2019/11/11 15:48
 */
public class Crust {
    // 缓存标识
    public static final String CATCH_NAME = "crustLightCache";
    // 缓存前辍
    public static final String CATCH_KEY_PREFIX = "crust:user:";

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
     * 权限列表
     */
    private static final String AUTHORITIES = "authorities";
    /**
     * 角色id
     */
    private static final String ROLE_IDS = "roles";

    @Autowired
    private CrustProperties props;

    @Resource
    Cache crustLightCache;

    /**
     * 登录认证
     *
     * @param username 用户名
     * @param password 密码
     * @param entityClazz 实体类型
     * @param <T> 实体类型
     * @return CrustUserInfo
     */
    public <T> CrustUserInfo<T> login(String username, String password, Class<T> entityClazz) {
        CrustAuthenticationToken authenticationToken = new CrustAuthenticationToken(username, password);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(WebContext.getRequest()));
        AuthenticationManager authenticationManager = ApplicationContextHolder.get().getBean(AuthenticationManager.class);
        // 执行登录认证过程
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        // 认证成功存储认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // token方式
        if (props.isStateless()) {
            // 生成令牌并返回给客户端
            return generateToken(authentication, entityClazz);
        }
        // session方式
        return getUserInfo(authentication, entityClazz);
    }

    /**
     * 刷新令牌
     * @return Token
     */
    public String refreshToken() {
        if (!props.isStateless()) return null;
        String refreshedToken;
        try {
            Claims claims = JwtUtil.parseToken(getToken(), getUnSignKey());
            claims.put(CREATED, new Date());
            refreshedToken = JwtUtil.generateToken(claims, getSignKey(), props.getExpire(), props.isUseRsa());
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

    /**
     * 从认证信息获取用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 获取登录的用户ID
     * @return user id
     */
    public String getUserId() {
        String token = getToken();
        String unSignKey = getUnSignKey();
        Claims claims = JwtUtil.parseToken(token, unSignKey);
        return String.valueOf(claims.get(UID));
    }

    /**
     * 获取当前用户信息
     *
     * @param entityClazz 实体类型
     * @param <T> 实体类型
     * @return  CrustUserInfo
     */
    public <T> CrustUserInfo<T> getUserInfo(Class<T> entityClazz) {
        // 检测超级缓存
        Spot<Serializable, CrustUserInfo<T>> spot = CacheHelper.get(crustLightCache);
        if (spot != null && spot.getData() != null) {
            return spot.getData();
        }

        CrustUserInfo<T> userInfo = getUserInfo(getAuthentication(), entityClazz);
        // session方式下，设置用户数据到超级缓存
        if (!props.isStateless()) {
            Spot<Serializable, CrustUserInfo<T>> sessionSpot = new Spot<>();
            sessionSpot.setView(userInfo.getUid());
            sessionSpot.setData(userInfo);
            CacheHelper.set(crustLightCache, sessionSpot);
        }
        return userInfo;
    }

    /**
     * 获取Spring Security上下文
     * @return SecurityContext
     */
    public SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

    /**
     * 使登录信息失效
     */
    public void invalidate() {
        CacheHelper.erase(getCache(), getUserInfo(Serializable.class).getUid(), id -> Crust.CATCH_KEY_PREFIX + id);
        SecurityContextHolder.clearContext();
    }

    /**
     * 获取Token发行时间
     * @return token issue time
     */
    long getTokenIssue() {
        String token = getToken();
        String unSignKey = getUnSignKey();
        Claims claims = JwtUtil.parseToken(token, unSignKey);
        return (long) claims.get(CREATED);
    }

    /**
     * 根据令牌进行认证
     */
    void checkAuthentication() {
        // 获取令牌并根据令牌获取登录认证信息
        Authentication authentication = getAuthenticationFromToken();
        // 设置登录认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 根据请求令牌获取登录认证信息
     *
     * @return Authentication
     */
    @SuppressWarnings("unchecked")
    Authentication getAuthenticationFromToken() {
        Authentication authentication = null;
        // 获取请求携带的令牌
        String token = getToken();
        if (token != null) {
            // 当前上下文认证信息不存在
            if (getAuthentication() == null) {
                String unSignKey = getUnSignKey();
                Claims claims = JwtUtil.parseToken(token, unSignKey);
                if (claims == null) {
                    return null;
                }
                String username = claims.getSubject();
                if (username == null) {
                    return null;
                }
                if (JwtUtil.isTokenExpired(token, unSignKey)) {
                    return null;
                }
                Object authorsObj = claims.get(AUTHORITIES);
                List<GrantedAuthority> authorities = null;
                if (authorsObj != null) {
                    authorities = Arrays.stream(((String) authorsObj).split(",")).map(GrantedAuthorityImpl::new).collect(Collectors.toList());
                }
                Object RoleIdsObj = claims.get(ROLE_IDS);
                List<Long> roleIds = null;
                if (RoleIdsObj != null) {
                    roleIds = Arrays.stream(((String) RoleIdsObj).split(",")).map(Long::parseLong).collect(Collectors.toList());
                }
                CrustUserDetails userDetails = new CrustUserDetails((String) claims.get(UID), username, authorities, roleIds);
                authentication = new CrustAuthenticationToken(userDetails, null, authorities, token);
            } else {
                // 当前上下文认证信息存在，验证token是否正确匹配
                if (validateToken(token, getUsername())) {
                    // 如果上下文中Authentication非空，且请求令牌合法，直接返回当前登录认证信息
                    authentication = getAuthentication();
                }
            }
        }
        return authentication;
    }

    /**
     * 获取缓存
     *
     * @return Cache
     */
    Cache getCache() {
        return crustLightCache;
    }

    /**
     * 验证令牌
     *
     * @param token    令牌
     * @param username 用户名
     * @return 验证是否成功
     */
    private Boolean validateToken(String token, String username) {
        String userName = getUsernameFromToken(token);
        if (StringUtils.isEmpty(userName)) return false;
        return (userName.equals(username) && !JwtUtil.isTokenExpired(token, getUnSignKey()));
    }

    /**
     * 登录成功后生成令牌
     *
     * @param authentication 认证对象
     * @param entityClazz 实体类型
     * @return CrustUserInfo
     */
    private <T> CrustUserInfo<T> generateToken(Authentication authentication, Class<T> entityClazz) {
        Map<String, Object> claims = new HashMap<>(6);
        CrustUserInfo<T> userInfo = getUserInfo(authentication, entityClazz);
        claims.put(UID, userInfo.getUid());
        claims.put(USERNAME, userInfo.getUsername());
        claims.put(CREATED, new Date());
        if (!CollectionUtils.isEmpty(authentication.getAuthorities())) {
            List<String> authors = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            claims.put(AUTHORITIES, StringUtils.arrayToCommaDelimitedString(authors.toArray()));
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CrustUserDetails) {
            List<Long> roleIds = ((CrustUserDetails) principal).getRoleIds();
            if (!CollectionUtils.isEmpty(roleIds)) {
                claims.put(ROLE_IDS, StringUtils.arrayToCommaDelimitedString(roleIds.toArray()));
            }
        }
        String token = JwtUtil.generateToken(claims, getSignKey(), props.getExpire(), props.isUseRsa());
        userInfo.setToken(token);
        return userInfo;
    }

    /**
     * 根据认证信息获取用户信息
     *
     * @param authentication 认证信息
     * @param clazz 实体类型
     * @param <T> 实体类型
     * @return CrustUserInfo
     */
    @SuppressWarnings("unchecked")
    private <T> CrustUserInfo<T> getUserInfo(Authentication authentication, Class<T> clazz) {
        CrustUserInfo<T> userInfo = null;
        if (authentication != null) {
            // 从Token解析的自定义设置的UsernamePasswordAuthenticationToken
            if (authentication instanceof CrustAuthenticationToken) {
                CrustAuthenticationToken authenticationToken = (CrustAuthenticationToken) authentication;
                String token = authenticationToken.getToken();
                Object principal = authentication.getPrincipal();
                if (principal instanceof CrustUserDetails) {
                    CrustUserDetails userDetails = (CrustUserDetails) principal;
                    String uid = userDetails.getUid();
                    List<Long> roleIds = userDetails.getRoleIds();
                    CrustUserDetailsService detailsService = ApplicationContextHolder.get().getBean(CrustUserDetailsService.class);
                    T entity = (T) detailsService.findEntityById(uid);
                    if (props.isEnableCache()) {
                        return CacheHelper.get(crustLightCache, new TypeReference<CrustUserInfo<T>>(){},
                                id -> CATCH_KEY_PREFIX + id.toString(),
                                id -> new CrustUserInfo<>(uid, authenticationToken.getName(), token, roleIds,  entity));
                    }
                    userInfo = new CrustUserInfo<>(uid, authenticationToken.getName(), token, roleIds, entity);
                    return userInfo;
                }
            }

            // 此时 authentication 就是内部封装的 UsernamePasswordAuthenticationToken
            Object principal = authentication.getPrincipal();
            if (principal instanceof CrustUserDetails) {
                CrustUserDetails userDetails = (CrustUserDetails) principal;
                userInfo = new CrustUserInfo<>(userDetails.getUid(), userDetails.getUsername(), getToken(), userDetails.getRoleIds(), (T) userDetails.getEntity());
            }
        }
        return userInfo;
    }


    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    private String getUsernameFromToken(String token) {
        String username;
        try {
            Claims claims = JwtUtil.parseToken(token, getUnSignKey());
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
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
     * 从请求中获取Token
     *
     * @return Token
     */
    public String getToken() {
        if (!props.isStateless()) return null;
        String token = WebContext.getRequest().getHeader(props.getTokenName());
        if (StringUtils.isEmpty(token)) return null;
        // 一般请求头Authorization的值会添加Bearer
        String tokenHead = "Bearer ";
        if (token.contains(tokenHead)) {
            token = token.substring(tokenHead.length());
        }
        return token;
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
}
