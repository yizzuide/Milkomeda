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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.yizzuide.milkomeda.molecule.MoleculeContext;
import com.github.yizzuide.milkomeda.molecule.core.agg.AggregateRoot;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.command.Command;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * The base {@link Aggregate} can record and recreate from {@link Event}.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 17:09
 */
@ToString
@Getter
@Slf4j
public abstract class Aggregate implements AggregateRoot {

    protected final Long aggregateId;

    protected int version;

    @JsonIgnore
    protected int baseVersion;

    @JsonIgnore
    protected final List<Event> changes = new ArrayList<>();

    protected Aggregate(@NonNull Long aggregateId, int version) {
        this.aggregateId = aggregateId;
        this.baseVersion = this.version = version;
    }

    public void loadFromHistory(List<Event> events) {
        if (!changes.isEmpty()) {
            throw new IllegalStateException("Aggregate has non-empty changes");
        }
        events.forEach(event -> {
            if (event.getVersion() <= version) {
                throw new IllegalStateException(
                        "Event version %s <= aggregate base version %s".formatted(
                                event.getVersion(), getNextVersion()));
            }
            apply(event);
            baseVersion = version = event.getVersion();
        });
    }

    protected int getNextVersion() {
        return version + 1;
    }

    @Override
    public Collection<Object> domainEvents() {
        return Arrays.asList(changes.toArray());
    }

    @Override
    public void clearDomainEvents() {
        changes.clear();
    }

    /**
     * The subclass must apply domain event change after perform command.
     * @param event domain event
     */
    protected void applyChange(Event event) {
        if (event.getVersion() != getNextVersion()) {
            throw new IllegalStateException(
                    "Event version %s doesn't match expected version %s".formatted(
                            event.getVersion(), getNextVersion()));
        }
        apply(event);
        changes.add(event);
        version = event.getVersion();
        MoleculeContext.getDomainEventBus().collect(this);
    }

    private void apply(Event event) {
        log.debug("Applying event {}", event);
        invoke(event, "apply");
    }

    public void process(Command command) {
        log.debug("Processing command {}", command);
        invoke(command, "process");
    }

    @SneakyThrows(InvocationTargetException.class)
    private void invoke(Object o, String methodName) {
        try {
            Method method = this.getClass().getMethod(methodName, o.getClass());
            method.invoke(this, o);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new UnsupportedOperationException(
                    "Aggregate %s doesn't support %s(%s)".formatted(
                            this.getClass(), methodName, o.getClass().getSimpleName()),
                    e);
        }
    }
}