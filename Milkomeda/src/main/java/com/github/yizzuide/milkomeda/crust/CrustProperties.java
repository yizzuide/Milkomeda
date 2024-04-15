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

import com.github.yizzuide.milkomeda.crust.api.EnableCrustApi;
import com.github.yizzuide.milkomeda.light.LightProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
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
 * @version 3.20.0
 * <br>
 * Create at 2019/11/11 15:51
 */
@Data
@ConfigurationProperties("milkomeda.crust")
public class CrustProperties {

    /**
     * Used token authorization is default, set false if you use the session mode.
     */
    private boolean stateless = true;

    /**
     * Login type config for used with {@link Crust#login(String, String, Class)}.
     * @since 3.15.0
     */
    private CrustLoginType loginType = CrustLoginType.PASSWORD;

    /**
     * Code verify expire time, the login type must be `CrustLoginType.CODE`.
     * @since 3.15.0
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration codeExpire = Duration.ofSeconds(60);

    /**
     * Code length, the login type must be `CrustLoginType.CODE`.
     * @since 3.20.0
     */
    private int codeLength = 6;

    /**
     * Code for test environment, the login type must be `CrustLoginType.CODE`.
     * @since 3.20.0
     */
    private String testCode = "000000";

    /**
     * Set false if you need custom config via {@link CrustConfigurerAdapter}.
     * @since 3.15.0
     */
    private boolean useAutoConfig = true;

    /**
     * Set false if you don't need to cache (not supported with {@link EnableCrustApi}). <br>
     * Note：In Session mode, only the super cache is enabled because the Session itself has Session scope cache.
     */
    private boolean enableCache = true;

    /**
     * Set light cache properties.
     * @since 3.20.0
     */
    @NestedConfigurationProperty
    private LightProperties cache = new LightProperties();

    /**
     * Set false if you need to get entity of user info immediately (not supported with {@link EnableCrustApi}).
     * @since 3.15.0
     */
    private boolean enableLoadEntityLazy = true;

    /**
     * Used RSA encryption (not supported with {@link EnableCrustApi}). <br>
     * Note：Must be config with <code>priKey</code> and <code>pubKey</code> if set this is true.
     */
    private boolean useRsa = false;

    /**
     * Used AES encryption (not supported with {@link EnableCrustApi}). <br>
     */
    private String secureKey;

    /**
     * RSA public key (not supported with {@link EnableCrustApi}). <br>
     */
    private String pubKey;

    /**
     * RSA private key (not supported with {@link EnableCrustApi}). <br>
     */
    private String priKey;

    /**
     * Token expire time.
     */
    @DurationUnit(ChronoUnit.MINUTES)
    private Duration expire = Duration.ofMinutes(30);

    /**
     *  Set token name in request header.
     */
    private String tokenName = "token";

    /**
     * Used Spring Security Bcrypt to encode password (not supported with {@link EnableCrustApi}). <br>
     * Bcrypt is able to add salt to an encrypted password, which can be extracted when decrypted.
     */
    private boolean useBcrypt = true;

    /**
     * Enable auto refresh token (not supported with {@link EnableCrustApi}). <br>
     */
    private boolean enableAutoRefreshToken = true;

    /**
     * The refreshing interval for the Token access to expire (not supported with {@link EnableCrustApi}). <br>
     */
    @DurationUnit(ChronoUnit.MINUTES)
    private Duration refreshTokenInterval = Duration.ofMinutes(5);

    /**
     * The refreshing token name in response header (not supported with {@link EnableCrustApi}). <br>
     */
    private String refreshTokenName = "Authorization";

    /**
     * The login request path needs allowed (not supported with {@link EnableCrustApi}). <br>
     */
    private String loginUrl = "/login";

    /**
     * The logout request path (not supported with {@link EnableCrustApi}). <br>
     */
    private String logoutUrl = "/logout";

    /**
     * The default permit urls.
     * @since 3.5.0
     */
    private List<String> permitUrls = Arrays.asList("/favicon.ico", "/druid/**", "/doc.html", "/webjars/**",
            "/swagger-resources/**", "/swagger-ui.html");

    /**
     * The addition permit urls.
     * @since 3.5.0
     */
    private List<String> additionPermitUrls;

    /**
     * The static resource permit urls.
     * @since 3.5.0
     */
    private List<String> allowStaticUrls;

    /**
     * The root path '/' redirect url (not supported with {@link EnableCrustApi}). <br>
     * @since 3.12.10
     */
    private String rootRedirect;

    /**
     * The Static resource path. <br>
     * Keep config in yml: <code>spring.resources.add-mappings=false</code>
     * @since 3.12.10
     */
    private String staticLocation = CrustURLMappingConfigurer.staticLocation;

    /**
     * Configure static resource mapping.
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
