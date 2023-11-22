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

package com.github.yizzuide.milkomeda.wormhole;

import java.io.Serializable;
import java.util.List;

/**
 * The {@link TransactionWorkBus} interface bridge link {@link ApplicationService},
 * which provide transaction operation for support single user case business in domain.
 *
 * @since 3.15.0
 * @author yizzuide
 * Create at 2023/07/13 11:27
 */
public interface TransactionWorkBus {
    /**
     * A save type of transaction.
     */
    int TRANSACTION_OPERATION_SAVE = 1;

    /**
     * An update type of transaction.
     */
    int TRANSACTION_OPERATION_UPDATE = 2;

    /**
     * A deleted type of transaction.
     */
    int TRANSACTION_OPERATION_DELETE = 3;

    /**
     * Find record with key id.
     * @param id    key id
     * @param entityClass entity class
     * @return  entity record
     * @param <T>   entity type
     */
    <T> T selectById(Serializable id, Class<T> entityClass);

    /**
     * Query record list with query example.
     * @param queryExample  query example
     * @param entityClass   entity class
     * @return  entity record list
     * @param <T>   entity type
     */
    <T> List<T> selectList(Object queryExample, Class<T> entityClass);

    /**
     * Perform transaction action.
     * @param operation transaction operation type
     * @param entity    data entity
     * @param <T>   entity type
     * @return effect count
     */
    <T> int perform(int operation, T entity);

    /**
     * Perform batch transaction action.
     * @param operation transaction operation type
     * @param entities  data entity list
     * @param <T>   entity type
     * @return effect count
     */
    <T> int performBatch(int operation, List<T> entities);

    /**
     * Set application service which work with.
     * @param applicationService application service
     */
    void setApplicationService(ApplicationService<?> applicationService);

    /**
     * Get application service which work with.
     * @return  application service
     * @param <A> application service type
     */
    <A extends ApplicationService<?>> A getApplicationService();
}
