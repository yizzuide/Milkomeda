/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.util.JwtUtil;
import com.github.yizzuide.milkomeda.util.Strings;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Token resolver used for crust.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2022/12/08 03:17
 */
@Data
@AllArgsConstructor
public class CrustTokenResolver {
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

    private CrustProperties props;

    /**
     * Generate token with meta data.
     * @param userInfo  CrustUserInfo
     * @param roleIds   login user role id list
     * @return  token
     * @param <T>   entity type
     */
    public <T extends CrustEntity> String generate(CrustUserInfo<T, CrustPermission> userInfo, String roleIds) {
        Map<String, Object> claims = new HashMap<>(8);
        claims.put(UID, userInfo.getUidLong());
        claims.put(USERNAME, userInfo.getUsername());
        claims.put(CREATED, new Date());
        if (StringUtils.hasText(roleIds)) {
            claims.put(ROLE_IDS, roleIds);
        }
        long expire = LocalDateTime.now().plusMinutes(props.getExpire().toMinutes()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String token = JwtUtil.generateToken(claims, getSignKey(), expire, props.isUseRsa());
        userInfo.setToken(token);
        userInfo.setTokenExpire(expire);
        return token;
    }

    /**
     * Parse token to get meta data.
     * @param token request token
     * @param lazyLoad  load entity with lazy callback
     * @return  CrustUserInfo
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public CrustUserInfo resolve(String token, @Nullable Function<Serializable, CrustEntity> lazyLoad) {
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
        CrustEntity entity;
        if (lazyLoad == null) {
            entity = null;
        } else {
            entity = getProps().isEnableLoadEntityLazy() ? null : lazyLoad.apply(uid);
        }
        CrustUserInfo userInfo = new CrustUserInfo<>(uid, username, token, roleIds, entity);
        userInfo.setTokenExpire(expire);
        return userInfo;
    }

    /**
     * Refresh token.
     * @param oldToken  original token
     * @param loginUserInfo CrustUserInfo
     * @return a new token with next expires
     */
    public String refresh(String oldToken, CrustUserInfo<?, CrustPermission> loginUserInfo) {
        Claims claims = JwtUtil.parseToken(oldToken, getUnSignKey());
        claims.put(CREATED, new Date());
        long expire = LocalDateTime.now().plusMinutes(props.getExpire().toMinutes()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String refreshedToken = JwtUtil.generateToken(claims, getSignKey(), expire, props.isUseRsa());
        loginUserInfo.setToken(refreshedToken);
        loginUserInfo.setTokenExpire(expire);
        return refreshedToken;
    }

    /**
     * Get token from request header.
     * @return  token
     */
    public String getRequestToken() {
        String token = WebContext.getRequestNonNull().getHeader(props.getTokenName());
        if (Strings.isEmpty(token)) { return null; }
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
