package com.github.yizzuide.milkomeda.crust;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CrustTokenMetaData
 *
 * @author yizzuide
 * @since 2.0.4
 * Create at 2019/12/27 16:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrustTokenMetaData {
    /**
     * 用户名
     */
    private String username;
    /**
     * 用户id
     */
    private String uid;

    /**
     * token发行时间
     */
    private long issuedAt;

    /**
     * 过期时间
     */
    private long expire;
}
