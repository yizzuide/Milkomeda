package com.github.yizzuide.milkomeda.crust;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * CrustUserInfo
 * 面向业务层使用的用户信息
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.17.3
 * Create at 2019/11/11 21:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrustUserInfo<T> implements Serializable {
    private static final long serialVersionUID = -3849153553850107966L;
    /**
     * 用户id
     */
    private String uid;
    /**
     * 用户名
     */
    private String username;
    /**
     * 认证token（stateless=true时有值）
     */
    private String token;
    /**
     * 角色id
     */
    private List<Long> roleIds;
    /**
     * 用户实体对象
     * <br>
     * 注意，这个有没有值根据下面条件：<br>
     * 如果stateless=false，那么是使用session方式，这个一定有实体对象<br>
     * 如果stateless=true，那么是无状态token认证方式，该值默认为null，如果想要有值，可实现：<br>
     * <pre class="code">
     * public class XXXDetailsService extends CrustUserDetailsService {
     *   protected Serializable findEntityById(String uid) {
     *      return new User("1000", "yiz", new BCryptPasswordEncoder().encode("123456"), null);
     *   }
     * }
     * </pre>
     * @see CrustUserDetailsService#findEntityById(String)
     */
    private T entity;

    /**
     * 获取第一个角色id
     * @return  角色id
     */
    @Nullable
    public Long firstRole() {
        if (CollectionUtils.isEmpty(roleIds)) {
            return null;
        }
        return getRoleIds().get(0);
    }

    /**
     * 获取第一个角色id
     * @return  角色id
     */
    public long firstRoleVal() {
        Long val = firstRole();
        assert val != null;
        return val;
    }
}
