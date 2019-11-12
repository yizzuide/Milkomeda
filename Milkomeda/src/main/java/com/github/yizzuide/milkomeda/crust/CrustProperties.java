package com.github.yizzuide.milkomeda.crust;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CrustProperties
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 15:51
 */
@Data
@ConfigurationProperties("milkomeda.crust")
public class CrustProperties {
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
     * Token过期时间（单位分，默认30分钟）
     */
    private Integer expire = 30;

    /**
     * 请求头自定义的token名（默认为token)
     */
    private String tokenName = "token";

    /**
     * 是否使用BCrypt，实现直接在密码里加salt（默认为true）
     * 什么是BCrypt？BCrypt能够将salt添加到加密的密码中，解密时可以将salt提取出来
     */
    private boolean useBCrypt = true;
}
