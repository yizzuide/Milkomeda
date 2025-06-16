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

package com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg;

import com.github.yizzuide.milkomeda.molecule.MoleculeContext;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.command.BindAggregateId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.command.Command;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.command.CreatedCommand;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.exception.AggregateStateException;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service.AggregateStore;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * This factory create {@link Aggregate} which annotated {@link AggregateType}.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 16:32
 */
public class AggregateFactory {

    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    public static <T extends Aggregate> T newInstance(String aggregateType, Long aggregateId) {
        Class<? extends Aggregate> aggregateClass = MoleculeContext.getClassByAggregateType(aggregateType);
        var constructor = aggregateClass.getDeclaredConstructor(Long.class, Integer.TYPE);
        return (T) constructor.newInstance(aggregateId, 0);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Aggregate> T create(AggregateStore aggregateStore, @Nullable String aggregateType, Command command) {
        if (aggregateType == null) {
            aggregateType = command.getAggregateType();
        }
        CreatedCommand createdCommand = AnnotationUtils.findAnnotation(command.getClass(), CreatedCommand.class);
        if (createdCommand != null) {
            return (T) aggregateStore.createAggregateFromType(aggregateType);
        }
        Long aggregateId = ReflectUtil.getAnnotatedFieldValue(BindAggregateId.class, command, Long.class);
        if (aggregateId == null) {
            throw new AggregateStateException("aggregate id must be not null");
        }
        return (T)aggregateStore.readAggregate(aggregateType, aggregateId);
    }
}