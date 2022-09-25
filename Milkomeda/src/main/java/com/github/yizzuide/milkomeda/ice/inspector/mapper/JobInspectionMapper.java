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

package com.github.yizzuide.milkomeda.ice.inspector.mapper;

import com.github.yizzuide.milkomeda.ice.inspector.domain.JobInspection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Job Inspection mybatis mapper
 *
 * @author yizzuide
 * @since 3.14.0
 * Create at 2022/09/25 17:25
 */
@Mapper
public interface JobInspectionMapper {

    /**
     * Query record row by id field.
     *
     * @param id primary key.
     * @return Job inspection instance.
     */
    JobInspection queryById(Long id);

    /**
     * Query record rows.
     *
     * @param jobInspection condition for query.
     * @param pageable     pagination query.
     * @param useUpdate    Is used update time for sort?
     * @param order        order of sort.
     * @return Job inspection list.
     */
    List<JobInspection> queryAllByLimit(@Param("entity") JobInspection jobInspection, @Param("pageable") Pageable pageable, @Param("useUpdate") boolean useUpdate, @Param("sortOrder") int order);

    /**
     * Sum of record rows.
     *
     * @param jobInspection condition for query.
     * @return count of record rows.
     */
    long count(JobInspection jobInspection);

    /**
     * Add new record row.
     *
     * @param jobInspection data for insert.
     * @return rows of effect.
     */
    int insert(JobInspection jobInspection);

    /**
     * Batch add rows.
     *
     * @param entities data for insert.
     * @return rows of effect.
     */
    int insertBatch(@Param("entities") List<JobInspection> entities);

    /**
     * Batch add rows or update by primary key.
     *
     * @param entities data for insert or update.
     * @return rows of effect.
     * @throws org.springframework.jdbc.BadSqlGrammarException When the input parameter is an empty List, an SQL statement error exception will be thrown.
     */
    int insertOrUpdateBatch(@Param("entities") List<JobInspection> entities);

    /**
     * Update a row.
     *
     * @param jobInspection data for update.
     * @return rows of effect.
     */
    int update(JobInspection jobInspection);

    /**
     * Remove record row by primary key.
     *
     * @param id  primary key
     * @return rows of effect.
     */
    int deleteById(Long id);

}

