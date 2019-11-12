package com.github.yizzuide.milkomeda.crust;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CrustUserDetailsService
 * 用户源数据提取服务抽象，需要继承实现从数据源提取数据
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 18:01
 */
public abstract class CrustUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CrustEntity entity = findEntityByUsername(username);
        if (entity == null) {
            throw new UsernameNotFoundException("Not Found: " + username);
        }
        // 用户权限列表，根据用户拥有的权限标识与如 @PreAuthorize("hasAuthority('sys:menu:view')") 标注的接口对比，决定是否可以调用接口
        Set<String> permissions = findPermissionsById(entity.getId());
        List<GrantedAuthority> grantedAuthorities = permissions.stream().map(GrantedAuthorityImpl::new).collect(Collectors.toList());
        return new CrustUserDetails(entity.getId(), entity.getUsername(), entity.getPassword(), entity.getSalt(), grantedAuthorities);
    }

    /**
     * 根据用户名查找实体
     * @param username  用户名
     * @return  CrustEntity
     */
    protected abstract CrustEntity findEntityByUsername(String username);

    /**
     * 根据用户名查找权限列表
     * @param uid  用户id
     * @return  权限列表
     */
    protected abstract Set<String> findPermissionsById(String uid);
}
