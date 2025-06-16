/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.uinterface.config;

import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.processor.DataSourceRouting;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service.EventSubscriptionProcessor;
import com.github.yizzuide.milkomeda.orbit.OrbitAdvice;
import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import com.github.yizzuide.milkomeda.sundial.DynamicRouteDataSource;
import com.github.yizzuide.milkomeda.sundial.SundialHolder;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * {@link EventSubscriptionProcessor} 切面通知，用于与当前多数据源环境集成
 *
 * @author yizzuide
 * Create at 2025/06/16 16:40
 */
public class DataSourceSelectAdvice implements OrbitAdvice {
    @Override
    public Object invoke(OrbitInvocation invocation) {
        DataSourceRouting annotation = AnnotationUtils.findAnnotation(invocation.getTargetClass(), DataSourceRouting.class);
        String routingKey = annotation == null ? DynamicRouteDataSource.MASTER_KEY : annotation.value();
        try {
            SundialHolder.setDataSourceType(routingKey);
            return invocation.proceed();
        } finally {
            SundialHolder.clearDataSourceType();
        }
    }
}
