package com.github.yizzuide.milkomeda.demo.crust.provider;

import com.github.yizzuide.milkomeda.crust.CrustEntity;
import com.github.yizzuide.milkomeda.crust.CrustUserDetailsService;
import com.github.yizzuide.milkomeda.demo.crust.pojo.User;
import org.assertj.core.util.Lists;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.Set;

/**
 * UserDetailsService
 *
 * @author yizzuide
 * Create at 2019/11/11 23:44
 */
public class UserDetailsService extends CrustUserDetailsService {

    @Override
    protected CrustEntity findEntityByUsername(String username) {
        // 实际情况下通过Dao查询，未找到，直接返回null
        // 模拟自定义salt方式
//        return new User("1000", username, new PasswordEncoder("111222").encode("123456"), "111222");
        // 模拟BCrypt方式
        return new User("1000", username, new BCryptPasswordEncoder().encode("123456"), null);
    }

    @Override
    protected Set<String> findPermissionsById(String uid) {
        // 实际情况下通过Dao查询
        return new HashSet<>(Lists.list("case:find:list", "case:find:one"));
    }
}
