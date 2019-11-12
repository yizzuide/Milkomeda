package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;
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
 * Create at 2019/11/11 18:02
 */
public class CrustAuthenticationProvider extends DaoAuthenticationProvider {

    CrustAuthenticationProvider(CrustUserDetailsService detailsService) {
        setUserDetailsService(detailsService);
        // 设置BCrypt密码加密器
        if (isUseBCrypt()) {
            setPasswordEncoder(ApplicationContextHolder.get().getBean(BCryptPasswordEncoder.class));
        }
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {

        // 如果使用BCrypt密码方式，使用父类默认实现
        if (isUseBCrypt()) {
            super.additionalAuthenticationChecks(userDetails, authentication);
            return;
        }

        // 自定义加salt实现
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        String presentedPassword = authentication.getCredentials().toString();
        String salt = ((CrustUserDetails) userDetails).getSalt();
        // 覆写密码验证逻辑
        if (!new PasswordEncoder(salt).matches(presentedPassword, userDetails.getPassword())) {
            logger.debug("Authentication failed: password does not match stored value");
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }

    private boolean isUseBCrypt() {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        CrustProperties props = applicationContext.getBean(CrustProperties.class);
        return props.isUseBCrypt();
    }
}
