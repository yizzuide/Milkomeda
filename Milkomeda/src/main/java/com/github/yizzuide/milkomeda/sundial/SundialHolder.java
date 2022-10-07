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

package com.github.yizzuide.milkomeda.sundial;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据源处理
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * @since 3.7.1
 * <br />
 * Create at 2020/5/8
 */
@Slf4j
public class SundialHolder {

    /**
     * 使用ThreadLocal维护变量，ThreadLocal为每个使用该变量的线程提供独立的变量副本，
     *  所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置数据源key
     * @param dsType 数据源key
     */
    public static void setDataSourceType(String dsType) {
        CONTEXT_HOLDER.set(dsType);
        log.debug("Sundial selected datasource key: {}", dsType);
    }

    /**
     * 获得数据源key
     * @return 数据源key
     */
    public static String getDataSourceType() {
        String type = CONTEXT_HOLDER.get();
        if (type == null) {
            return DynamicRouteDataSource.MASTER_KEY;
        }
        return type;
    }

    /**
     * 清空数据源key
     */
    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }
}
