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

package com.github.yizzuide.milkomeda.sirius.wormhole;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yizzuide.milkomeda.sirius.BatchMapper;
import com.github.yizzuide.milkomeda.universe.exception.NotImplementException;
import com.github.yizzuide.milkomeda.wormhole.ApplicationService;
import com.github.yizzuide.milkomeda.wormhole.TransactionWorkBus;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Mybatis plus impl of {@link TransactionWorkBus}.
 *
 *
 * @since 3.15.0
 * @author yizzuide
 * Create at 2023/07/13 11:44
 */
public class SiriusTransactionWorkBus implements TransactionWorkBus {

    /**
     * Batch insert operation with primary key.
     */
    @Setter
    private boolean useBatchInsertWithKey;

    /**
     * Link application service which belong to.
     */
    @Setter
    private ApplicationService<?> applicationService;

    public SiriusTransactionWorkBus(boolean useBatchInsertWithKey) {
        this.useBatchInsertWithKey = useBatchInsertWithKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends ApplicationService<?>> A getApplicationService() {
        return (A) applicationService;
    }

    @Override
    public <T> T selectById(Serializable id, Class<T> entityClass) {
        BaseMapper<T> mapper = SiriusInspector.getMapper(entityClass);
        return mapper.selectById(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> selectList(Object queryExample, Class<T> entityClass) {
        if (!(queryExample instanceof QueryWrapper)) {
            throw new NotImplementException("The queryExample must impl with QueryWrapper for select operation.");
        }
        BaseMapper<T> mapper = SiriusInspector.getMapper(entityClass);
        return mapper.selectList((QueryWrapper<T>) queryExample);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> int perform(int operation, T entity) {
        BaseMapper<T> mapper = (BaseMapper<T>) SiriusInspector.getMapper(entity.getClass());
        switch (operation) {
            case TRANSACTION_OPERATION_SAVE:
                return mapper.insert(entity);
            case TRANSACTION_OPERATION_UPDATE:
                return mapper.updateById(entity);
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> int performBatch(int operation, List<T> entities) {
        BaseMapper<T> mapper = (BaseMapper<T>) SiriusInspector.getMapper(entities.get(0).getClass());
        if (!(mapper instanceof BatchMapper)) {
            throw new NotImplementException("The mapper must impl with BatchMapper for batch operation.");
        }
        BatchMapper<T> batchMapper = (BatchMapper<T>) mapper;
        switch (operation) {
            case TRANSACTION_OPERATION_SAVE:
                return useBatchInsertWithKey ? batchMapper.insertKeyBatch(entities) : batchMapper.insertBatch(entities);
            case TRANSACTION_OPERATION_UPDATE:
                return batchMapper.updateBatchById(entities);
        }
        return 0;
    }
}
