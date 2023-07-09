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
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.apache.ibatis.session.SqlSession;

/**
 * An inspector for mybatis and mybatis-plus.
 *
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 * @see org.apache.ibatis.session.SqlSession
 * @see org.apache.ibatis.session.SqlSessionManager
 * @see com.baomidou.mybatisplus.core.MybatisMapperRegistry
 * @see org.mybatis.spring.SqlSessionUtils
 * @see com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils
 * @since 3.15.0
 * @author yizzuide
 * Create at 2023/07/09 15:52
 */
public class SiriusInspector {

    /**
     * Get mapper with entity class when sql session has opened before.
     * @param entityClass   entity class
     * @return  mapper
     * @param <T>   entity type
     */
    public static  <T> BaseMapper<T> getMapper(Class<T> entityClass) {
        SqlSession sqlSession = SqlHelper.sqlSession(entityClass);
        return SqlHelper.getMapper(entityClass, sqlSession);
    }

}
