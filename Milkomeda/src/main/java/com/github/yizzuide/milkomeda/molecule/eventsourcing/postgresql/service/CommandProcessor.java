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
import com.github.yizzuide.milkomeda.molecule.core.event.RecordAggregateEvent;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.Aggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.AggregateFactory;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.command.Command;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.commandhandler.CommandHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.commandhandler.DefaultCommandHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventWithId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.processor.DataSourceRouting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * This processor handles commands and saves the {@link Aggregate} events with {@link AggregateStore} synchronously.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 19:36
 */
@DataSourceRouting("pg")
@RequiredArgsConstructor
@Slf4j
public class CommandProcessor {

    private final AggregateStore aggregateStore;
    private final List<CommandHandler<? extends Command>> commandHandlers;
    private final DefaultCommandHandler defaultCommandHandler;

    public Aggregate process(@NonNull Command command) {
        log.debug("Processing command {}", command);
        Aggregate aggregate = AggregateFactory.create(aggregateStore, null, command);

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
        return aggregate;
    }

    @Transactional(rollbackFor = Throwable.class)
    @EventListener
    public void handle(RecordAggregateEvent ignore) {
        List<Aggregate> aggregates = MoleculeContext.getDomainEventBus().getHangingAggregates(Aggregate.class);
        try {
            if (aggregates.isEmpty()) {
                return;
            }
            aggregates.forEach(this::saveAndSendEvents);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.debug("aggregates and events commit, clear hanging aggregates");
                }
            });
        } catch (Exception ex) {
            aggregates.forEach(agg -> aggregateStore.deleteTempAggregate(agg.getAggregateId()));
            throw ex;
        } finally {
            MoleculeContext.getDomainEventBus().clear(null);
        }
    }

    private void saveAndSendEvents(Aggregate aggregate) {
        List<EventWithId<Event>> newEvents = aggregateStore.saveAggregate(aggregate);
        String aggregateType = MoleculeContext.getAggregateTypeByClass(aggregate.getClass());
        MoleculeContext.getSyncEventHandlers(aggregateType)
                .forEach(handler -> handler.handleEvents(newEvents, aggregate));
    }
}