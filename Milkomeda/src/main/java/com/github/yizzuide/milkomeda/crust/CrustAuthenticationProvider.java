package com.github.yizzuide.milkomeda.crust;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * CrustAuthenticationProvider
 * 认证提供者
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.16.4
 * Create at 2019/11/11 18:02
 */
public class CrustAuthenticationProvider extends DaoAuthenticationProvider {

    private CrustProperties props;

    public CrustAuthenticationProvider(CrustProperties props, BCryptPasswordEncoder passwordEncoder) {
        this.props = props;
        // 设置BCrypt密码加密器
        if (props.isUseBcrypt() && passwordEncoder != null) {
            setPasswordEncoder(passwordEncoder);
        }
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {

        // 如果使用BCrypt密码方式，使用父类默认实现
        if (props.isUseBcrypt()) {
            super.additionalAuthenticationChecks(userDetails, authentication);
            return;
        }

        // 检查登录密码
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        boolean isMatched;
        String presentedPassword = authentication.getCredentials().toString();
        // 如果用户有实现自定义加密器
        if (getPasswordEncoder() != null) {
            isMatched = getPasswordEncoder().matches(presentedPassword, userDetails.getPassword());
        } else {
            // 否则使用内置加密器
            String salt = ((CrustUserDetails) userDetails).getSalt();
            isMatched = new PasswordEncoder(salt).matches(presentedPassword, userDetails.getPassword());
        }

        // 如果验证失败
        if (!isMatched) {
            logger.debug("Authentication failed: password does not match stored value");
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }
}
