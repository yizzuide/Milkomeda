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

package com.github.yizzuide.milkomeda.util;

import io.jsonwebtoken.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * JwtUtil
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.20.0
 * <br>
 * Create at 2019/11/11 15:16
 */
public class JwtUtil {
    /**
     * 私钥加密token
     *
     * @param claims        载荷中的数据
     * @param privateKey    私钥
     * @param expireMinutes 过期时间，单位分钟
     * @return Token
     */
    public static String generateToken(Map<String, Object> claims, PrivateKey privateKey, int expireMinutes) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(LocalDateTime.now().plusMinutes(expireMinutes).atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    /**
     * 生成Token
     * @param claims        载荷中的数据
     * @param secureKey     密钥
     * @param expireTime    过期时间
     * @param usedRSA       是否使用RSA加密方式
     * @return  Token
     */
    public static String generateToken(Map<String, Object> claims, String secureKey, long expireTime, boolean usedRSA) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(Instant.ofEpochMilli(expireTime)))
                .signWith(usedRSA ? SignatureAlgorithm.RS256 : SignatureAlgorithm.HS256, secureKey)
                .compact();
    }

    /**
     * 解析token
     *
     * @param token     用户请求中的token
     * @param publicKey 公钥
     * @return Claims
     * @throws ExpiredJwtException token过期
     * @throws UnsupportedJwtException 不支持
     * @throws MalformedJwtException 被篡改
     * @throws IllegalArgumentException 包含非法参数
     */
    public static Claims parseToken(String token, PublicKey publicKey) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SignatureException, IllegalArgumentException {
        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token).getBody();
    }

    /**
     * 解析token
     *
     * @param token     用户请求中的token
     * @param secureKey 加密密钥
     * @return Claims
     * @throws ExpiredJwtException token过期
     * @throws UnsupportedJwtException 不支持
     * @throws MalformedJwtException 被篡改
     * @throws IllegalArgumentException 包含非法参数
     */
    public static Claims parseToken(String token, String secureKey) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, IllegalArgumentException {
        return Jwts.parser().setSigningKey(secureKey).parseClaimsJws(token).getBody();
    }

    /**
     * 判断令牌是否过期
     * @param token 令牌
     * @param publicKey 公钥
     * @return 是否过期
     */
    public static Boolean isTokenExpired(String token, PublicKey publicKey) {
        try {
            Claims claims = parseToken(token, publicKey);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断令牌是否过期
     * @param token 令牌
     * @param secureKey 密钥
     * @return 是否过期
     */
    public static Boolean isTokenExpired(String token, String secureKey) {
        Claims claims = parseToken(token, secureKey);
        return isTokenExpired(claims);
    }

    /**
     * 判断令牌是否过期
     * @param claims Token数据
     * @return 是否过期
     * @since 3.14.0
     */
    public static Boolean isTokenExpired(Claims claims) {
        try {
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

}
