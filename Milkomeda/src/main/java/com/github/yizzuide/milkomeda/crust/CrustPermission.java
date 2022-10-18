/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

import org.springframework.beans.BeanUtils;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Permission for front-end request.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/16 16:35
 */
public interface CrustPermission extends Ordered {
    /**
     * Set permission id.
     * @param id Long
     */
    void setId(Long id);

    /**
     * Permission id.
     * @return Long
     */
    Long getId();

    /**
     * Set permission parent id.
     * @param parentId Long
     */
    void setParentId(Long parentId);

    /**
     * Permission parent id.
     * @return  Long
     */
    Long getParentId();

    /**
     * Set permission name.
     * @param label String
     */
    void setLabel(String label);

    /**
     * Permission name.
     * @return  String
     */
    String getLabel();

    /**
     * Set permission icon.
     * @param icon  String
     */
    void setIcon(String icon);

    /**
     * Permission icon.
     * @return String
     */
    String getIcon();

    /**
     * Set permission code.
     * @param code String
     */
    void setCode(String code);

    /**
     * Permission code.
     * @return String
     */
    String getCode();

    /**
     * Set permission type.
     * @param type 0 is menu directory, 1 is menu, 2 is button action
     */
    void setType(Integer type);

    /**
     * Permission type. (0 is menu directory, 1 is menu, 2 is button action)
     * @return  Integer
     */
    Integer getType();

    /**
     * Set front-end route path.
     * @param routePath String
     */
    void setRoutePath(String routePath);

    /**
     * Front-end route path.
     * @return  String
     */
    String getRoutePath();

    /**
     * Set front-end route name.
     * @param routeName String
     */
    void setRouteName(String routeName);

    /**
     * Front-end route name.
     * @return  String
     */
    String getRouteName();

    /**
     * Set permission access resource url.
     * @param url String
     */
    void setUrl(String url);

    /**
     * Component resource url.
     * @return String
     */
    String getUrl();

    /**
     * Set permission order.
     * @param order int
     */
    void setOrder(int order);

    /**
     * Set sub-permission list.
     * @param children  sub-permission list
     */
    void setChildren(List<CrustPermission> children);

    /**
     * Get sub-permission list.
     * @return  sub-permission list
     */
    List<CrustPermission> getChildren();

    /**
     * build perm tree from permission list.
     * @param permissionList    permission list
     * @param parentId          build start parent id
     * @return menu tree
     */
    static List<CrustPermission> buildPermTree(List<CrustPermission> permissionList, @NonNull Class<?> permClass, Long parentId) {
        if (CollectionUtils.isEmpty(permissionList)) {
            return null;
        }
        return permissionList.stream().filter(perm -> perm.getParentId() == null ? parentId == null : perm.getParentId().equals(parentId))
                .map(perm -> {
                    CrustPermission newPerm = null;
                    try {
                        newPerm = (CrustPermission) permClass.newInstance();
                    } catch (Exception ignore) {}
                    Assert.notNull(newPerm, "Create instance error with class: " + permClass.getTypeName());
                    BeanUtils.copyProperties(perm, newPerm);
                    return newPerm;
                })
                .peek(perm -> perm.setChildren(buildPermTree(permissionList, permClass, perm.getId())))
                .sorted(OrderComparator.INSTANCE.withSourceProvider(p -> p))
                .collect(Collectors.toList());
    }

    /**
     * build menu tree from permission list.
     * @param permissionList    permission list
     * @param parentId          build start parent id
     * @return menu tree
     */
    static List<CrustMenu> buildMenuTree(List<CrustPermission> permissionList, Long parentId) {
        if (CollectionUtils.isEmpty(permissionList)) {
            return null;
        }
        return permissionList.stream().filter(perm -> perm.getParentId() == null ?
                        parentId == null : perm.getParentId().equals(parentId) && perm.getType() != 2)
                .map(perm -> {
                    CrustMenu menu = CrustMenu.buildOf(perm);
                    List<CrustMenu> subMenuList = buildMenuTree(permissionList, perm.getId());
                    menu.setChildren(subMenuList);
                    return menu;
                }).sorted(OrderComparator.INSTANCE.withSourceProvider(m -> m)).collect(Collectors.toList());
    }
}
