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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * CrustProperties
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.15.0
 * <br>
 * Create at 2019/11/11 15:51
 */
@Data
@ConfigurationProperties("milkomeda.crust")
public class CrustProperties {
    /**
     * 使用Token的无状态登录（后台session管理方式设置为false）
     */
    private boolean stateless = true;

    /**
     * Login type config for used with {@link Crust#login(String, String, Class)}.
     * @since 3.15.0
     */
    private CrustLoginType loginType = CrustLoginType.PASSWORD;

    /**
     * Code verify expire time, must set `CrustLoginType.CODE` with property of {@link CrustProperties#loginType}.
     * @since 3.15.0
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration codeExpire = Duration.ofSeconds(60);

    /**
     * Set false if you need custom config via {@link CrustConfigurerAdapter}.
     * @since 3.15.0
     */
    private boolean useAutoConfig = true;

    /**
     * Set false if you use stateless token.
     * 认证缓存（Session方式下仅开启超级缓存，因为Session本身有Session级缓存）
     */
    private boolean enableCache = true;

    /**
     * Set true if cache only used memory.
     */
    private boolean cacheInMemory = false;

    /**
     * Set false if you need get entity of user info immediately.
     * @since 3.15.0
     */
    private boolean enableLoadEntityLazy = true;

    /**
     * 使用非对称方式（默认为false)<br>
     * 注意：如果设置true，则必须设置<code>priKey</code>和<code>pubKey</code>
     */
    private boolean useRsa = false;

    /**
     * 对称密钥
     */
    private String secureKey;

    /**
     * 非对称公钥
     */
    private String pubKey;

    /**
     * 非对称私钥
     */
    private String priKey;

    /**
     * Token过期时间（默认30分钟，单位：分）
     */
    @DurationUnit(ChronoUnit.MINUTES)
    private Duration expire = Duration.ofMinutes(30);

    /**
     * 请求头自定义的token名（默认为token)
     */
    private String tokenName = "token";

    /**
     * 是否使用Bcrypt，实现直接在密码里加salt（默认为true）<br>
     * 什么是Bcrypt？Bcrypt能够将salt添加到加密的密码中，解密时可以将salt提取出来
     */
    private boolean useBcrypt = true;

    /**
     * 开启Token自动刷新（默认开启）
     */
    private boolean enableAutoRefreshToken = true;

    /**
     * Token访问即将过期的刷新间隔
     */
    @DurationUnit(ChronoUnit.MINUTES)
    private Duration refreshTokenInterval = Duration.ofMinutes(5);

    /**
     * Token刷新响应字段
     */
    private String refreshTokenName = "Authorization";

    /**
     * 登录页面路径（仅在stateless=false时有效，默认/login）
     */
    private String loginUrl = "/login";

    /**
     * 登出路径（默认/logout）
     */
    private String logoutUrl = "/logout";

    /**
     * 默认允许访问的URL
     * @since 3.5.0
     */
    private List<String> permitUrls = Arrays.asList("/favicon.ico", "/druid/**", "/doc.html", "/webjars/**",
            "/swagger-resources/**", "/swagger-ui.html");

    /**
     * 添加允许访问的URL
     * @since 3.5.0
     */
    private List<String> additionPermitUrls;

    /**
     * 允许的静态资源
     * @since 3.5.0
     */
    private List<String> allowStaticUrls;

    /**
     * 根路径跳转
     * @since 3.12.10
     */
    private String rootRedirect;

    /**
     * 静态资源路径
     * 需要配置<code>spring.resources.add-mappings=false</code>
     * @since 3.12.10
     */
    private String staticLocation = CrustConfig.CrustURLMappingConfigurer.staticLocation;

    /**
     * 配置静态资源映射
     * @since 3.14.0
     */
    private List<ResourceMapping> resourceMappings;

    @Data
    public static class ResourceMapping {
        /**
         * Path pattern list.
         * @since 3.14.0
         */
        private List<String> pathPatterns;
        /**
         * Target location list.
         * @since 3.14.0
         */
        private List<String> targetLocations;

    }
}
