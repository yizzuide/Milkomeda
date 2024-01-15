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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * CrustUserInfo
 * 面向业务层使用的用户信息
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.14.0
 * <br>
 * Create at 2019/11/11 21:51
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"entity"})
@Data
public class CrustUserInfo<T, P> implements Serializable {
    @Serial
    private static final long serialVersionUID = -3849153553850107966L;

    /**
     * 用户id
     */
    protected Serializable uid;

    /**
     * 用户名
     */
    protected String username;

    /**
     * 认证token（stateless=true时有值）
     */
    protected String token;

    /**
     * token过期时间
     * @since 3.14.0
     */
    protected Long tokenExpire;

    /**
     * Check is admin user.
     * @since 3.14.0
     */
    private Boolean isAdmin;

    /**
     * 角色id列表
     */
    private List<Long> roleIds;

    /**
     * 权限列表
     * @since 3.14.0
     */
    private List<P> permissionList;

    /**
     * 用户实体对象，它的值根据以下条件确定：<br>
     * 1.如果stateless=false，那么使用的是session方式，这个一定有实体对象<br>
     * 2.如果stateless=true，那么是无状态token认证方式，该值默认为null，如果想要有值，可实现：<br>
     * <pre class="code">
     * public class XXXDetailsService extends CrustUserDetailsService {
     *   protected CrustEntity findEntityById(String uid) {
     *   }
     * }
     * </pre>
     * @see CrustUserDetailsService#findEntityById(Serializable)
     */
    protected T entity;

    /**
     * User entity current class.
     * @since 3.14.0
     */
    @JsonIgnore
    private Class<?> entityClass;

    /**
     * Permission class type.
     * @since 3.14.0
     */
    private Class<?> permClass;

    public CrustUserInfo(Serializable uid, String username, String token, List<Long> roleIds, T entity) {
        this.uid = uid;
        this.username = username;
        this.token = token;
        this.roleIds = roleIds;
        this.entity = entity;
    }

    /**
     * Get string type user id.
     * @return user id
     */
    public Serializable getUid() {
        return this.uid.toString();
    }

    /**
     * Get long type user id.
     * @return user id
     * @since 3.14.0
     */
    @JsonIgnore
    public Long getUidLong() {
        if (this.uid instanceof Long) {
            return (Long) this.uid;
        }
        return Long.parseLong(this.uid.toString());
    }

    /**
     * Get user entity.
     * @return user entity
     */
    @SuppressWarnings("unchecked")
    public T getEntity() {
        AbstractCrust crust = CrustContext.get();
        if (crust != null && crust.getProps().isEnableLoadEntityLazy() && this.entity == null) {
            this.entity = crust.loadEntity(this.getUid());
        }
        if (this.entity instanceof Map) {
            if (this.getEntityClass() == null) {
                return this.entity;
            }
            this.entity = (T) JSONUtil.parse(JSONUtil.serialize(this.entity), this.getEntityClass());
        }
        return this.entity;
    }

    @SuppressWarnings("unchecked")
    public void setPermissionList(List<P> permissionList) {
        if (!CollectionUtils.isEmpty(permissionList)) {
            // user has no permissions!
            if (permissionList.getFirst() == null) {
                return;
            }
            boolean isMap = permissionList.getFirst() instanceof Map;
            if (isMap && this.permClass != null) {
                this.permissionList = (List<P>) JSONUtil.parseList(JSONUtil.serialize(permissionList), this.permClass);
                return;
            }
            this.permClass = permissionList.getFirst().getClass();
        }
        this.permissionList = permissionList;
    }

    public void setPermClass(Class<?> permClass) {
        this.permClass = permClass;
        if (this.permissionList != null) {
            this.setPermissionList(this.permissionList);
        }
    }

    /**
     * 获取第一个角色id
     * @return  角色id
     */
    @Nullable
    public Long firstRole() {
        if (CollectionUtils.isEmpty(this.getRoleIds())) {
            return null;
        }
        return getRoleIds().getFirst();
    }
}
