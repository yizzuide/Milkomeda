package com.github.yizzuide.milkomeda.crust;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

/**
 * GrantedAuthorityImpl
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 17:40
 */
public class GrantedAuthorityImpl implements GrantedAuthority {
    private static final long serialVersionUID = 5254047441946093520L;

    @Getter @Setter
    private String authority;

    public GrantedAuthorityImpl(String authority) {
        this.authority = authority;
    }
}
