package com.github.yizzuide.milkomeda.crust;

import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CrustUserDetailsService
 * 用户源数据提取服务抽象，需要继承实现从数据源提取数据
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.17.3
 * Create at 2019/11/11 18:01
 */
public abstract class CrustUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CrustEntity entity = findEntityByUsername(username);
        if (entity == null) {
            throw new UsernameNotFoundException("Not Found: " + username);
        }
        CrustPerm crustPerm = findPermissionsById(entity.getUID(), username);
        List<GrantedAuthority> grantedAuthorities = null;
        List<Long> roleIds = null;
        if (crustPerm != null) {
            if (!CollectionUtils.isEmpty(crustPerm.getRoleIds())) {
                roleIds = new ArrayList<>(crustPerm.getRoleIds());
            }
            if (!CollectionUtils.isEmpty(crustPerm.getPermNames())) {
                grantedAuthorities = crustPerm.getPermNames().stream().map(GrantedAuthorityImpl::new).collect(Collectors.toList());
            }
        }
        return new CrustUserDetails(entity.getUID(), entity.getUsername(), entity.getPassword(),
                entity.getSalt(), grantedAuthorities, roleIds, entity);
    }

    /**
     * 根据用户id查找实体（用于配置stateless=true时查询实体）
     *
     * @param uid   用户id
     * @return  CrustEntity
     */
    @Nullable
    protected Serializable findEntityById(String uid) {return null;}

    /**
     * 根据用户名查找实体
     *
     * @param username  用户名
     * @return  CrustEntity
     */
    @Nullable
    protected abstract CrustEntity findEntityByUsername(String username);

    /**
     * 根据用户名查找权限列表
     *
     * @param uid  用户id
     * @param username 用户名
     * @return  权限数据
     */
    @Nullable
    protected abstract CrustPerm findPermissionsById(String uid, String username);
}
