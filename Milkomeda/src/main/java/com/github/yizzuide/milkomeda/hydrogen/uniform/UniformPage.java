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

package com.github.yizzuide.milkomeda.hydrogen.uniform;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Function;

/**
 * Page data for {@link UniformResult}.
 *
 * @since 3.14.0
 * @version 4.0.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/29 19:35
 */
@Data
public class UniformPage<T> {

    /**
     * Record row total size.
     */
    private Long totalSize;

    /**
     * Total number of pages.
     */
    private Long pageCount;

    /**
     * Record rows list.
     */
    private List<T> list;

    /**
     * Convert to a new UniformPage with the specified VO type.
     * @param uniformPage   page result
     * @param converter     convert from entity to VO
     * @return  UniformPage
     * @param <T> entity type
     * @param <V> vo type
     * @since 4.0.0
     */
    public static <T, V> UniformPage<V> convert(UniformPage<T> uniformPage, Function<T, V> converter) {
        UniformPage<V> cloneUniformPage = new UniformPage<>();
        cloneUniformPage.setTotalSize(uniformPage.getTotalSize());
        cloneUniformPage.setPageCount(uniformPage.getPageCount());
        if (!CollectionUtils.isEmpty(uniformPage.getList())) {
            cloneUniformPage.setList(uniformPage.getList().stream().map(converter).toList());
        }
        return cloneUniformPage;
    }
}
