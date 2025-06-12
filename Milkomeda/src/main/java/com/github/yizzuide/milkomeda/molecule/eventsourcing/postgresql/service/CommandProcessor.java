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
import com.github.yizzuide.milkomeda.molecule.core.event.ApplicationPostCommitEvent;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.Aggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.command.Command;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.commandhandler.CommandHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.commandhandler.DefaultCommandHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventWithId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.SyncEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This processor handles commands and saves the {@link Aggregate} events with {@link AggregateStore} synchronously.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 19:36
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommandProcessor {

    private final AggregateStore aggregateStore;
    private final List<CommandHandler<? extends Command>> commandHandlers;
    private final DefaultCommandHandler defaultCommandHandler;
    private final List<SyncEventHandler> aggregateChangesHandlers;

    public Aggregate process(@NonNull Command command) {
        log.debug("Processing command {}", command);

        String aggregateType = command.getAggregateType();
        Long aggregateId = command.getAggregateId();

        Aggregate aggregate = aggregateStore.readAggregate(aggregateType, aggregateId);

        commandHandlers.stream()
                .filter(commandHandler -> commandHandler.getCommandType() == command.getClass())
                .findFirst()
                .ifPresentOrElse(commandHandler -> {
                    log.debug("Handling command {} with {}",
                            command.getClass().getSimpleName(), commandHandler.getClass().getSimpleName());
                    commandHandler.handle(aggregate, command);
                }, () -> {
                    log.debug("No specialized handler found, handling command {} with {}",
                            command.getClass().getSimpleName(), defaultCommandHandler.getClass().getSimpleName());
                    defaultCommandHandler.handle(aggregate, command);
                });

        saveAndSendEvents(aggregate);
        return aggregate;
    }

    @EventListener
    public void handle(ApplicationPostCommitEvent ignore) {
        List<Aggregate> aggregates = MoleculeContext.getDomainEventBus().getHangingAggregates(Aggregate.class);
        if (aggregates.isEmpty()) {
            return;
        }
        aggregates.forEach(this::saveAndSendEvents);
        MoleculeContext.getDomainEventBus().clear(null);
    }

    public void saveAndSendEvents(Aggregate aggregate) {
        List<EventWithId<Event>> newEvents = aggregateStore.saveAggregate(aggregate);
        String aggregateType = MoleculeContext.getAggregateTypeByClass(aggregate.getClass());
        MoleculeContext.getSyncEventHandlers(aggregateType)
                .forEach(handler -> handler.handleEvents(newEvents, aggregate));
    }
}