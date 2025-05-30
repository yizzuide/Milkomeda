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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 用户验证的源数据
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.17.1
 * <br>
 * Create at 2019/11/11 17:23
 */
@Data
@AllArgsConstructor
public class CrustUserDetails implements UserDetails {
    
    private static final long serialVersionUID = 2749178892063846690L;

    /**
     * 登录用户ID
     */
    private Serializable uid;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 可选自定义加密盐数据列
     */
    private String salt;

    /**
     * 角色id列表
     */
    private List<Long> roleIds;

    /**
     * 角色权限
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 用户信息
     */
    private CrustUserInfo<CrustEntity, CrustPermission> userInfo;

    CrustUserDetails(Serializable uid, String username, Collection<? extends GrantedAuthority> authorities, List<Long> roleIds) {
        this.uid = uid;
        this.username = username;
        this.authorities = authorities;
        this.roleIds = roleIds;
    }

    @Override
    public boolean isAccountNonExpired() {
        CrustEntity entity = userInfo.getEntity();
        if (entity instanceof CrustStatefulEntity) {
            return !((CrustStatefulEntity) entity).accountExpired();
        }
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        CrustEntity entity = userInfo.getEntity();
        if (entity instanceof CrustStatefulEntity) {
            return !((CrustStatefulEntity) entity).accountLocked();
        }
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        CrustEntity entity = userInfo.getEntity();
        if (entity instanceof CrustStatefulEntity) {
            return !((CrustStatefulEntity) entity).credentialsExpired();
        }
        return true;
    }

    @Override
    public boolean isEnabled() {
        CrustEntity entity = userInfo.getEntity();
        if (entity instanceof CrustStatefulEntity) {
            return ((CrustStatefulEntity) entity).enabled();
        }
        return true;
    }
}
