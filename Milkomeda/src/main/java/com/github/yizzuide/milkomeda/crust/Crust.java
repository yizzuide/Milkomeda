package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.light.Cache;
import com.github.yizzuide.milkomeda.light.CacheHelper;
import com.github.yizzuide.milkomeda.light.LightCacheable;
import com.github.yizzuide.milkomeda.light.Spot;
import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
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
 * @version 2.0.0
 * Create at 2019/11/11 15:48
 */
public class Crust {
    // 缓存标识
    static final String CATCH_NAME = "crustLightCache";
    // 缓存前辍
    private static final String CATCH_KEY_PREFIX = "crust:user:";

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

    @Getter
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
    @NonNull
    public <T> CrustUserInfo<T> login(@NonNull String username, @NonNull String password, @NonNull Class<T> entityClazz) {
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
        return getLoginUserInfo(authentication, entityClazz);
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
     * 获取当前用户信息
     *
     * @param entityClazz 实体类型
     * @param <T> 实体类型
     * @return  CrustUserInfo
     */
    @NonNull
    public <T> CrustUserInfo<T> getUserInfo(@NonNull Class<T> entityClazz) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("authentication is null");
        }

        // Session方式下
        if (!props.isStateless()) {
            // 先检测超级缓存（提高性能）
            Spot<Serializable, CrustUserInfo<T>> spot = CacheHelper.get(crustLightCache);
            if (spot != null && spot.getData() != null) {
                return spot.getData();
            }
            CrustUserInfo<T> userInfo = getLoginUserInfo(authentication, entityClazz);
            // 设置用户数据到超级缓存（在内存中已经有一份用户登录元数据了）
            Spot<Serializable, CrustUserInfo<T>> sessionSpot = new Spot<>();
            sessionSpot.setView(userInfo.getUid());
            sessionSpot.setData(userInfo);
            CacheHelper.set(crustLightCache, sessionSpot);
            return userInfo;
        }

        // Token方式
        Crust crustProxy = AopContextHolder.self(this.getClass());
        return crustProxy.getTokenUserInfo(authentication, entityClazz);
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
    @NonNull
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
    @NonNull
    private Boolean validateToken(@NonNull String token, @NonNull String username) {
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
    @NonNull
    private <T> CrustUserInfo<T> generateToken(@NonNull Authentication authentication, @NonNull Class<T> entityClazz) {
        Map<String, Object> claims = new HashMap<>(6);
        CrustUserInfo<T> userInfo = getLoginUserInfo(authentication, entityClazz);
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

    @LightCacheable(value = Crust.CATCH_NAME, keyPrefix = CATCH_KEY_PREFIX, key = "T(org.springframework.util.DigestUtils).md5DigestAsHex(#authentication?.token.bytes)", // #authentication 与 args[0] 等价
            condition = "#authentication!=null&&#target.props.enableCache") // #target 与 @crust、#this.object、#root.object（#this在表达式不同部分解析过程中可能会改变，但是#root总是指向根，，object为自定义root对象属性）等价
    @SuppressWarnings("unchecked")
    public <T> CrustUserInfo<T> getTokenUserInfo(Authentication authentication, @NonNull Class<T> clazz) {
        CrustUserInfo<T> userInfo = null;
        if (authentication != null) {
            // 解析Token方式获取用户信息
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
                    return new CrustUserInfo<>(uid, authenticationToken.getName(), token, roleIds,  entity);
                }
            }
        }
        return userInfo;
    }

    /**
     * 获取登录时用户信息
     * <br>
     * Session方式和Token方式登录时都会调用（Session方式获取用户信息也走这里）
     *
     * @param authentication    认证信息
     * @param clazz             实体类型
     * @param <T>               实体类型
     * @return  CrustUserInfo
     */
    @SuppressWarnings("unchecked")
    private <T> CrustUserInfo<T> getLoginUserInfo(Authentication authentication, @NonNull Class<T> clazz) {
        if (authentication == null) {
            throw new AuthenticationServiceException("Authentication is must be not null");
        }
        // 此时 authentication 就是内部封装的 UsernamePasswordAuthenticationToken
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CrustUserDetails)) {
            throw new AuthenticationServiceException("Authentication principal must be subtype of CrustUserDetails");
        }
        CrustUserDetails userDetails = (CrustUserDetails) principal;
        return new CrustUserInfo<>(userDetails.getUid(), userDetails.getUsername(), getToken(), userDetails.getRoleIds(), (T) userDetails.getEntity());
    }




    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    @Nullable
    private String getUsernameFromToken(@NonNull String token) {
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
    @Nullable
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
