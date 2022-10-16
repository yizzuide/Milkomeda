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

import java.io.Serializable;
import java.util.List;

/**
 * CrustEntity
 * 需要用户实体实现的适配接口
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 2.0.4
 * <br>
 * Create at 2019/11/11 18:47
 */
public interface CrustEntity extends Serializable {
    /**
     * 用户id
     * @return user id
     */
    Serializable getUID();

    /**
     * 用户名
     * @return username
     */
    String getUsername();

    /**
     * 登录密码
     * @return password
     */
    String getPassword();

    /**
     * 如果设置了<code>milkomeda.crust.use_bCrypt</code>为<code>true</code>(默认为true)，
     * 那个这个字段不需要实现，否则需要实现
     * @return salt
     */
    default String getSalt() {return null;}

    /**
     * Set permission list.
     * @param permissionList    permission list
     */
    default void setPermissionList(List<? extends CrustPermission> permissionList) {}
}
