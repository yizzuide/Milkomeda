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
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色与权限Code
 *
 * @author yizzuide
 * @since 1.17.2
 * <br>
 * Create at 2019/12/06 17:17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrustPerm {
    /**
     * 是否为Admin用户
     * @since 3.14.0
     */
    private boolean isAdmin;

    /**
     * 角色ID列表
     */
    private Set<Long> roleIds;

    /**
     * 权限列表
     * @since 3.14.0
     */
    private List<CrustPermission> permissionList;

    public static Builder builder() {
        return new Builder(new CrustPerm());
    }

    public static class Builder {

        private final CrustPerm crustPerm;

        public Builder(CrustPerm crustPerm) {
            this.crustPerm = crustPerm;
        }

        public Builder admin(Boolean isAdmin) {
            this.crustPerm.setAdmin(isAdmin);
            return this;
        }

        public Builder roleIds(List<Long> roleIds) {
            this.crustPerm.setRoleIds(new HashSet<>(roleIds));
            return this;
        }

        public Builder permissionList(List<? extends CrustPermission> permissionList) {
            this.crustPerm.setPermissionList(permissionList.stream().map(perm -> (CrustPermission)perm).collect(Collectors.toList()));
            return this;
        }

        public CrustPerm build() {
            return this.crustPerm;
        }

    }

    /**
     * Build spring security authorities.
     * @return authorities list.
     * @since 3.14.0
     */
    public static List<GrantedAuthority> buildAuthorities(List<? extends CrustPermission> permissionList) {
        if (permissionList == null) {
            return null;
        }
        List<String> codeList = permissionList.stream().map(CrustPermission::getCode).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(codeList)) {
            return null;
        }
        return codeList.stream().map(GrantedAuthorityImpl::new).collect(Collectors.toList());
    }
}
