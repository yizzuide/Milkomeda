package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Crust
 * 核心API
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 15:48
 */
public class Crust {
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
    private static final String CREATED = "created";
    /**
     * 权限列表
     */
    private static final String AUTHORITIES = "authorities";

    @Autowired
    private CrustProperties props;

    /**
     * 登录认证
     *
     * @param username 用户名
     * @param password 密码
     * @return CrustUserInfo
     */
    public CrustUserInfo login(String username, String password) {
        CrustAuthenticationToken authenticationToken = new CrustAuthenticationToken(username, password);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(WebContext.getRequest()));
        AuthenticationManager authenticationManager = ApplicationContextHolder.get().getBean(AuthenticationManager.class);
        // 执行登录认证过程
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        // 认证成功存储认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 生成令牌并返回给客户端
        return generateToken(authentication);
    }

    /**
     * 刷新令牌
     * @return Token
     */
    public String refreshToken() {
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
     * @return  CrustUserInfo
     */
    public CrustUserInfo getUserInfo() {
        return getUserInfo(getAuthentication());
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
     * @return 用户名
     */
    private Authentication getAuthenticationFromToken() {
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
                Object authors = claims.get(AUTHORITIES);
                List<GrantedAuthority> authorities = new ArrayList<>();
                if (authors instanceof List) {
                    for (Object object : (List) authors) {
                        authorities.add(new GrantedAuthorityImpl((String) ((Map) object).get("authority")));
                    }
                }
                CrustUserDetails userDetails = new CrustUserDetails((String) claims.get(UID), username, authorities);
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
     * @return CrustUserInfo
     */
    private CrustUserInfo generateToken(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>(6);
        CrustUserInfo userInfo = getUserInfo(authentication);
        claims.put(UID, userInfo.getUid());
        claims.put(USERNAME, userInfo.getUsername());
        claims.put(CREATED, new Date());
        claims.put(AUTHORITIES, authentication.getAuthorities());
        String token = JwtUtil.generateToken(claims, getSignKey(), props.getExpire(), props.isUseRsa());
        userInfo.setToken(token);
        return userInfo;
    }

    /**
     * 根据认证信息获取用户信息
     *
     * @return CrustUserInfo
     */
    private CrustUserInfo getUserInfo(Authentication authentication) {
        CrustUserInfo userInfo = null;
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CrustUserDetails) {
                CrustUserDetails userDetails = (CrustUserDetails) principal;
                userInfo = new CrustUserInfo(userDetails.getUid(), userDetails.getUsername(), getToken());
            } else {
                CrustAuthenticationToken authenticationToken = (CrustAuthenticationToken) authentication;
                Claims claims = JwtUtil.parseToken(authenticationToken.getToken(), getUnSignKey());
                String uid = (String) claims.get(UID);
                userInfo = new CrustUserInfo(uid, authenticationToken.getName(), authenticationToken.getToken());
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
    private String getToken() {
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
     */
    private String getSignKey() {
        if (props.isUseRsa()) {
            return props.getPriKey();
        }
        return props.getSecureKey();
    }

    /**
     * 获取解密key
     */
    private String getUnSignKey() {
        if (props.isUseRsa()) {
            return props.getPubKey();
        }
        return props.getSecureKey();
    }
}
