package com.github.yizzuide.milkomeda.crust;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * CrustAuthenticationToken
 * 支持JWT token的认证令牌
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 17:12
 */
public class CrustAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private static final long serialVersionUID = -4832533804982166177L;
    /**
     * 访问令牌
     */
    @Getter @Setter
    private String token;

    CrustAuthenticationToken(Object principal, Object credentials){
        super(principal, credentials);
    }

    public CrustAuthenticationToken(Object principal, Object credentials, String token){
        super(principal, credentials);
        this.token = token;
    }

    CrustAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String token) {
        super(principal, credentials, authorities);
        this.token = token;
    }
}
