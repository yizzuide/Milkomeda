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

/**
 * Stateful crust entity that corresponds to spring security `UserDetails`.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/15 13:35
 */
public interface CrustStatefulEntity extends CrustEntity {

    /**
     * The account is expired.
     * @return ture is expired
     */
    boolean accountExpired();

    /**
     * The account is locked.
     * @return ture is locked
     */
    boolean accountLocked();

    /**
     * The credentials are expired.
     * @return ture is expired
     */
    boolean credentialsExpired();

    /**
     * The account is enabled.
     * @return ture is enabled
     */
    boolean enabled();
}
