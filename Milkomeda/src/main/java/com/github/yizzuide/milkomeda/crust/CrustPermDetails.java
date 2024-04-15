/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The details of {@link CrustPerm}.
 *
 * @since 3.20.0
 * @author yizzuide
 * Create at 2024/01/22 14:53
 */
@Builder
@Data
public class CrustPermDetails {
    /**
     * Fill the roles with the user.
     */
    private BiConsumer<CrustUserInfo<CrustEntity, CrustPermission>, List<Long>> rolesCollector;

    /**
     * Filter out roles which used to query permissions.
     */
    private Function<List<Long>, List<Long>> rolesFilter;

    /**
     * Recognize the user is admin.
     */
    private Function<List<Long>, Boolean> adminRecognizer;

    /**
     * Query the permissions with the user.
     */
    private BiFunction<List<Long>, Boolean, List<? extends CrustPermission>> permsCollector;
}
