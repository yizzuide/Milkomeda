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

package com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yizzuide.milkomeda.molecule.MoleculeContext;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.command.Command;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.commandhandler.CommandHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.commandhandler.DefaultCommandHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.AsyncEventHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.SyncEventHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.processor.PostgresChannelEventSubscriptionProcessor;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.processor.ScheduledEventSubscriptionProcessor;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.repository.AggregateRepository;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.repository.EventRepository;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.repository.EventSubscriptionRepository;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service.AggregateStore;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service.CommandProcessor;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service.EventSubscriptionProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

/**
 * Event sourcing configuration.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/10 16:16
 */
@ConditionalOnProperty(prefix = EventSourcingProperties.PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties(EventSourcingProperties.class)
@Configuration(proxyBeanMethods = false)
public class EventSourcingConfig {

    @ConditionalOnProperty(prefix = EventSourcingProperties.PREFIX, name = "subscription-type", havingValue = "polling")
    @Bean
    public ScheduledEventSubscriptionProcessor scheduledEventSubscriptionProcessor(
            List<AsyncEventHandler> eventHandlers,
            EventSubscriptionProcessor eventSubscriptionProcessor
    ) {
        MoleculeContext.loadAsyncEventHandlers(eventHandlers);
        return new ScheduledEventSubscriptionProcessor(eventHandlers, eventSubscriptionProcessor);
    }

    @ConditionalOnProperty(prefix = EventSourcingProperties.PREFIX, name = "subscription-type", havingValue = "postgres_channel", matchIfMissing = true)
    @Bean
    public PostgresChannelEventSubscriptionProcessor postgresChannelEventSubscriptionProcessor(
            List<AsyncEventHandler> eventHandlers,
            EventSubscriptionProcessor eventSubscriptionProcessor,
            EventSourcingProperties properties
    ) {
        MoleculeContext.loadAsyncEventHandlers(eventHandlers);
        return new PostgresChannelEventSubscriptionProcessor(eventHandlers, eventSubscriptionProcessor, properties);
    }

    @Bean
    public EventRepository eventRepository(
            NamedParameterJdbcTemplate jdbcTemplate,
            @Qualifier("jacksonObjectMapper") ObjectMapper objectMapper
    ) {
        return new EventRepository(jdbcTemplate, objectMapper);
    }

    @Bean
    public EventSubscriptionProcessor eventSubscriptionProcessor(
            EventRepository eventRepository,
            EventSubscriptionRepository eventSubscriptionRepository
    ) {
        return new EventSubscriptionProcessor(eventSubscriptionRepository, eventRepository);
    }

    @Bean
    public DefaultCommandHandler commandHandler() {
        return new DefaultCommandHandler();
    }

    @Bean
    public EventSubscriptionRepository eventSubscriptionRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        return new EventSubscriptionRepository(jdbcTemplate);
    }

    @Bean
    public AggregateRepository aggregateRepository(
            NamedParameterJdbcTemplate jdbcTemplate,
            @Qualifier("jacksonObjectMapper") ObjectMapper objectMapper
    ) {
        return new AggregateRepository(jdbcTemplate, objectMapper);
    }

    @Bean
    public AggregateStore aggregateStore(
            AggregateRepository aggregateRepository,
            EventRepository eventRepository,
            EventSourcingProperties properties
    ) {
        return new AggregateStore(aggregateRepository, eventRepository, properties);
    }

    @Bean
    public CommandProcessor commandProcessor(
            AggregateStore aggregateStore,
            List<CommandHandler<? extends Command>> commandHandlers,
            DefaultCommandHandler commandHandler,
            List<SyncEventHandler> aggregateChangesHandlers
    ) {
        MoleculeContext.loadSyncEventHandlers(aggregateChangesHandlers);
        return new CommandProcessor(aggregateStore, commandHandlers, commandHandler);
    }
}
