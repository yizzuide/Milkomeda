/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Extend base mapper to support batch insert and update. <br>
 * Note: Must add `allowMultiQueries=true` in datasource connect url params.
 *
 * @since 3.15.0
 * @author yizzuide
 * Create at 2023/07/13 14:30
 */
public interface BatchMapper<T> extends BaseMapper<T> {

    /**
     * Batch insert record with the primary key.
     * @param list  record list
     * @return  insert effect count
     */
    int insertKeyBatch(@Param("list") List<T> list);

    /**
     * Batch insert record without the primary key.
     * @param list  record list
     * @return  insert effect count
     */
    int insertBatch(@Param("list") List<T> list);

    /**
     * Batch update record with id key.
     * @param list  record list
     * @return  update effect count
     */
    int updateBatchById(@Param("list") List<T> list);
}
