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

package com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service;

import com.github.yizzuide.milkomeda.molecule.MoleculeContext;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.Aggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.BindAggregateId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.command.Command;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.command.CreatedCommand;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * The base application service which provides aggregate methods.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/12 13:33
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ApplicationService {

    @Autowired
    private AggregateStore aggregateStore;

    @SuppressWarnings("unchecked")
    protected <T extends Aggregate> T loadAggregate(Class<T> aggregateClass, Command command) {
        String aggregateType = MoleculeContext.getAggregateTypeByClass(aggregateClass);
        command.setAggregateType(aggregateType);
        CreatedCommand createdCommand = AnnotationUtils.findAnnotation(command.getClass(), CreatedCommand.class);
        if (createdCommand != null) {
            return (T) aggregateStore.createAggregateFromType(aggregateType);
        }
        Long aggregateId = ReflectUtil.getAnnotatedFieldValue(BindAggregateId.class, command, Long.class);
        return (T)aggregateStore.readAggregate(aggregateType, aggregateId);
    }

}
