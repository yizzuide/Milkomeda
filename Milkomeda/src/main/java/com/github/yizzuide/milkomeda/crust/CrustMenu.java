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

import lombok.Builder;
import lombok.Data;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * CrustMenu
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/19 00:24
 */
@Builder
@Data
public class CrustMenu implements Ordered {
    /**
     * Menu name.
     */
    private String name;

    /**
     * Menu icon.
     */
    private String icon;

    /**
     * Menu route path.
     */
    private String routePath;

    /**
     * Menu route name.
     */
    private String routeName;

    /**
     * Menu route component path.
     */
    private String componentPath;

    /**
     * Menu order.
     */
    private int order;

    /**
     * Sub-menu list.
     */
    private List<CrustMenu> children;

    /**
     * Build from permission.
     * @param perm  permission
     * @return  CrustMenu
     */
    public static CrustMenu buildOf(CrustPermission perm) {
        return CrustMenu.builder().name(perm.getLabel()).icon(perm.getIcon())
                .routeName(perm.getRouteName()).routePath(perm.getRoutePath())
                .componentPath(perm.getUrl()).order(perm.getOrder()).build();
    }
}
