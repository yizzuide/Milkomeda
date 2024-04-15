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
 * 用户源数据提取服务抽象，需要继承实现从数据源提取数据
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.20.0
 * <br>
 * Create at 2019/11/11 18:01
 */
public abstract class CrustUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        CrustEntity entity = findEntityByUsername(account);
        if (entity == null) {
            throw new UsernameNotFoundException("Not found entity with account: " + account);
        }
        CrustUserInfo<CrustEntity, CrustPermission> userInfo = new CrustUserInfo<>();
        userInfo.setEntity(entity);
        userInfo.setUid(entity.getUid());
        userInfo.setUsername(entity.getUsername());

        CrustPerm crustPerm = findPermissions(userInfo);
        List<GrantedAuthority> grantedAuthorities = null;
        if (crustPerm != null) {
            userInfo.setIsAdmin(crustPerm.isAdmin());
            if (!CollectionUtils.isEmpty(crustPerm.getRoleIds())) {
                userInfo.setRoleIds(crustPerm.getRoleIds());
            }
            List<CrustPermission> permissionList = crustPerm.getPermissionList();
            userInfo.setPermissionList(permissionList);
            grantedAuthorities = CrustPerm.buildAuthorities(permissionList);
        }
        return new CrustUserDetails(userInfo.getUid(), userInfo.getUsername(), entity.getPassword(),
                entity.getSalt(), userInfo.getRoleIds(), grantedAuthorities, userInfo);
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
     * @param userInfo  filled with simple basic info
     * @return  权限数据
     */
    @Nullable
    protected CrustPerm findPermissions(CrustUserInfo<CrustEntity, CrustPermission> userInfo) {
        CrustPermDetails crustPermDetails = buildPremDetails();
        List<Long> roleIds = userInfo.getRoleIds();
        if (CollectionUtils.isEmpty(roleIds)) {
            roleIds = new ArrayList<>();
            crustPermDetails.getRolesCollector().accept(userInfo, roleIds);
        }
        List<Long> sysRoleIds = crustPermDetails.getRolesFilter() != null ?
                crustPermDetails.getRolesFilter().apply(roleIds) : roleIds;
        boolean isAdmin = crustPermDetails.getAdminRecognizer().apply(sysRoleIds);
        List<? extends CrustPermission> permissions = crustPermDetails.getPermsCollector().apply(sysRoleIds, isAdmin);
        return CrustPerm.builder().roleIds(roleIds).admin(isAdmin).permissionList(permissions).build();
    }

    /**
     * 登录时根据用户名查找权限明细（Session + Token）
     * @return CrustPermDetails
     * @since 3.20.0
     */
    protected abstract CrustPermDetails buildPremDetails();

    /**
     * 解析Token时根据用户id查找实体（Token）
     *
     * @param uid   用户id
     * @return  CrustEntity
     */
    @Nullable
    protected CrustEntity findEntityById(Serializable uid) {return null;}
}
