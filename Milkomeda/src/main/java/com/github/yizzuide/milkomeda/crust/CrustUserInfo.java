package com.github.yizzuide.milkomeda.crust;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CrustUserInfo
 * 面向业务层使用的用户信息
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 21:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrustUserInfo {
    private String uid;
    private String username;
    private String token;
}
