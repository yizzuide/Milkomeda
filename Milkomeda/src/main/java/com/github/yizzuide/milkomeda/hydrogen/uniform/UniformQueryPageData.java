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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.function.Function;

/**
 * Request query page type data.
 *
 * @since 3.14.0
 * @version 4.0.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/29 19:10
 */
@Schema(name = "queryPageData", description = "查询分页数据")
@EqualsAndHashCode(callSuper = true)
@Data
public class UniformQueryPageData<T> extends UniformQueryData<T> {
    /**
     * 查询当前页
     */
    @Schema(description = "查询当前页（从1开始）", defaultValue = "1")
    private Integer pageStart = 1;

    /**
     * 每页记录数
     */
    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;

    /**
     * 排序（1:asc, -1:desc)
     */
    @Schema(description = "redis分页排序", defaultValue = "1", hidden = true, accessMode = Schema.AccessMode.WRITE_ONLY)
    private Integer order = 1;

    /**
     * Convert query to the entity of page query.
     * @param queryPageData page query data
     * @param converter     query to entity
     * @return  UniformQueryPageData
     * @param <T>   entity type
     * @param <Q>   query type
     * @since 4.0.0
     */
    public static <T, Q> UniformQueryPageData<T> convert(UniformQueryPageData<Q> queryPageData, Function<Q, T> converter) {
        UniformQueryPageData<T> cloneQueryPageData = new UniformQueryPageData<>();
        cloneQueryPageData.setPageStart(queryPageData.getPageStart());
        cloneQueryPageData.setPageSize(queryPageData.getPageSize());
        cloneQueryPageData.setOrder(queryPageData.getOrder());
        cloneQueryPageData.setEntity(converter.apply(queryPageData.getEntity()));
        cloneQueryPageData.setStartDate(queryPageData.getStartDate());
        cloneQueryPageData.setEndDate(queryPageData.getEndDate());
        return cloneQueryPageData;
    }
}
