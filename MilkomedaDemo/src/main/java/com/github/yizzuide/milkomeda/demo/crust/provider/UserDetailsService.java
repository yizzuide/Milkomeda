package com.github.yizzuide.milkomeda.demo.crust.provider;

import com.github.yizzuide.milkomeda.crust.CrustEntity;
import com.github.yizzuide.milkomeda.crust.CrustPerm;
import com.github.yizzuide.milkomeda.crust.CrustUserDetailsService;
import com.github.yizzuide.milkomeda.demo.crust.pojo.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collections;

/**
 * UserDetailsService
 *
 * @author yizzuide
 * <br>
 * Create at 2019/11/11 23:44
 */
@Service
public class UserDetailsService extends CrustUserDetailsService {

    @Override
    protected CrustEntity findEntityByUsername(String username) {
        // 实际情况下通过Dao查询，未找到，直接返回null
        // 模拟自定义salt方式
//        return new User("1000", username, new PasswordEncoder("111222").encode("123456"), "111222");
        // 模拟BCrypt方式（BCrypt会把salt隐藏在密码里面）
        return new User("1000", username, new BCryptPasswordEncoder().encode("123456"), null);
    }

    @Override
    protected CrustPerm findPermissionsById(Serializable uid) {
        // 实际情况下通过Dao查询
        return CrustPerm.builder().permissionList(Collections.singletonList(CrustDefaultPermission.createRole("ADMIN"))).build();
    }

    @Override
    protected CrustEntity findEntityById(Serializable uid) {
        return new User("1000", "yiz", new BCryptPasswordEncoder().encode("123456"), null);
    }
}
