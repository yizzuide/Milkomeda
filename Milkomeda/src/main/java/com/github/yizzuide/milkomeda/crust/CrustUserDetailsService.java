/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

/**
 * CrustUserDetailsService
 * 用户源数据提取服务抽象，需要继承实现从数据源提取数据
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.17.3
 * <br>
 * Create at 2019/11/11 18:01
 */
public abstract class CrustUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CrustEntity entity = findEntityByUsername(username);
        if (entity == null) {
            throw new UsernameNotFoundException("Not found entity with username: " + username);
        }
        CrustUserInfo<CrustEntity, CrustPermission> userInfo = new CrustUserInfo<>();
        CrustPerm crustPerm = findPermissionsById(entity.getUid());
        List<GrantedAuthority> grantedAuthorities = null;
        List<Long> roleIds = null;
        if (crustPerm != null) {
            if (!CollectionUtils.isEmpty(crustPerm.getRoleIds())) {
                roleIds = new ArrayList<>(crustPerm.getRoleIds());
                userInfo.setRoleIds(roleIds);
            }
            List<CrustPermission> permissionList = crustPerm.getPermissionList();
            userInfo.setPermissionList(permissionList);
            grantedAuthorities = CrustPerm.buildAuthorities(permissionList);
        }
        userInfo.setEntity(entity);
        userInfo.setUid(entity.getUid());
        userInfo.setUsername(entity.getUsername());
        return new CrustUserDetails(userInfo.getUid(), userInfo.getUsername(), entity.getPassword(),
                entity.getSalt(), roleIds, grantedAuthorities, userInfo);
    }

    /**
     * 登录时根据用户名查找实体（Session + Token）
     *
     * @param username  用户名
     * @return  CrustEntity
     */
    @Nullable
    protected abstract CrustEntity findEntityByUsername(String username);

    /**
     * 登录时根据用户名查找权限列表（Session + Token）
     *
     * @param uid  用户id
     * @return  权限数据
     */
    @Nullable
    protected abstract CrustPerm findPermissionsById(Serializable uid);

    /**
     * 解析Token时根据用户id查找实体（Token）
     *
     * @param uid   用户id
     * @return  CrustEntity
     */
    @Nullable
    protected CrustEntity findEntityById(Serializable uid) {return null;}
}
