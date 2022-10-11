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

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * SundialTweakConfig
 *
 * @author yizzuide
 * @since 3.7.1
 * @version 3.12.10
 * <br>
 * Create at 2020/05/31 14:57
 */
@ConditionalOnClass(MybatisAutoConfiguration.class)
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class SundialTweakConfig {
    @Autowired
    public void configSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        MultiDataSourceTransactionFactory multiDataSourceTransactionFactory = new MultiDataSourceTransactionFactory();
        Configuration configuration = sqlSessionFactory.getConfiguration();
        // 以前使用反射路径方式（现在已有公开的API可以设置）
        // Pair<Field, Object> envFieldBundle = ReflectUtil.getFieldBundlePath(sqlSessionFactory, "configuration.environment");
        Environment originEnvironment = configuration.getEnvironment();
        Environment environment = new Environment(originEnvironment.getId(), multiDataSourceTransactionFactory, originEnvironment.getDataSource());
        configuration.setEnvironment(environment);
    }
}
