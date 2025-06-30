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

package com.github.yizzuide.milkomeda.molecule;

import com.github.yizzuide.milkomeda.molecule.core.event.DomainEventBus;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.AggregateType;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.EventSourcingProperties;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventType;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.Aggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.AsyncEventHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.SyncEventHandler;
import com.github.yizzuide.milkomeda.universe.context.SpringContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.*;

/**
 * The context hold {@link DomainEventBus} which manage domain events.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/09 19:56
 */
public class MoleculeContext {

    @Getter
    private static DomainEventBus domainEventBus;

    @Setter
    private static Map<String, Class<? extends Aggregate>> aggregateTypeMapper = new HashMap<>(64);

    private static final Map<Class<?  extends Aggregate>, String> aggregateTagMapper = new HashMap<>(64);

    @Setter
    private static Map<String, Class<? extends Event>> eventTypeMapper = new HashMap<>(64);

    private static final Map<Class<? extends Event>, String> eventTagMapper = new HashMap<>(64);

    @Setter
    private static Map<String, List<SyncEventHandler>> syncEventHandlerMapper = new HashMap<>(64);

    @Setter
    private static Map<String, List<AsyncEventHandler>> asyncEventHandlerMapper = new HashMap<>(64);

    private static final Map<Class<? extends AsyncEventHandler>, String> asyncEventHandlerTagMapper = new HashMap<>(64);

    static void setDomainEventBus(DomainEventBus domainEventBus) {
        MoleculeContext.domainEventBus = domainEventBus;
    }

    public static Class<? extends Aggregate> getClassByAggregateType(String aggregateType) {
        Class<? extends Aggregate> aggClass = aggregateTypeMapper.get(aggregateType);
        if (aggClass == null) {
            throw new IllegalArgumentException("Aggregate type " + aggregateType + " not found");
        }
        return aggClass;
    }

    public static String getAggregateTypeByClass(Class<? extends Aggregate> aggClass) {
        String aggregateType = aggregateTagMapper.get(aggClass);
        if (aggregateType == null) {
            throw new IllegalArgumentException("Aggregate class " + aggClass.getName() + " not found");
        }
        return aggregateType;
    }

    public static Class<? extends Event> getClassByEventType(String eventType) {
        Class<? extends Event> eventClass = eventTypeMapper.get(eventType);
        if (eventClass == null) {
            throw new IllegalArgumentException("Event type " + eventType + " not found");
        }
        return eventClass;
    }

    public static String getEventTypeByClass(Class<? extends Event> eventClass) {
        String eventType = eventTagMapper.get(eventClass);
        if (eventType == null) {
            throw new IllegalArgumentException("Event class " + eventClass.getName() + " not found");
        }
        return eventType;
    }

    public static List<SyncEventHandler> getSyncEventHandlers(String aggregateType) {
        return syncEventHandlerMapper.get(aggregateType);
    }

    public static List<AsyncEventHandler> getAsyncEventHandlers(String aggregateType) {
        return asyncEventHandlerMapper.get(aggregateType);
    }

    public static String getAsyncEventHandlerTypeByClass(Class<? extends AsyncEventHandler> asyncEventHandlerClass) {
        return asyncEventHandlerTagMapper.get(asyncEventHandlerClass);
    }

    public static void loadAggregatesAndEvents(ConfigurableEnvironment environment) {
        EventSourcingProperties properties =  Binder.get(environment).bind(EventSourcingProperties.PREFIX, EventSourcingProperties.class).orElseGet(EventSourcingProperties::new);
        if (properties == null || !properties.getEnabled()) {
            return;
        }
        Set<Class<Aggregate>> aggClasses = SpringContext.loadClassFromBasePackage(properties.getAggregatePackage(), Aggregate.class);
        Map<String, Class<? extends Aggregate>> aggregateClassMapper = SpringContext.bindClassTagMap(aggClasses, AggregateType.class, annotation -> ((AggregateType) annotation).value());
        MoleculeContext.setAggregateTypeMapper(aggregateClassMapper);
        aggregateClassMapper.forEach((key, value) -> aggregateTagMapper.put(value, key));
        Set<Class<Event>> eventClasses = SpringContext.loadClassFromBasePackage(properties.getEventPackage(), Event.class);
        Map<String, Class<? extends Event>> eventClassMapper = SpringContext.bindClassTagMap(eventClasses, EventType.class, annotation -> ((EventType) annotation).value());
        MoleculeContext.setEventTypeMapper(eventClassMapper);
        eventClassMapper.forEach((key, value) -> eventTagMapper.put(value, key));
    }

    public static void loadAsyncEventHandlers(List<AsyncEventHandler> eventHandlers) {
        eventHandlers.forEach(eventHandler -> {
            AggregateType aggregateType = AnnotationUtils.findAnnotation(eventHandler.getClass(), AggregateType.class);
            if (aggregateType != null) {
                asyncEventHandlerTagMapper.put(eventHandler.getClass(), aggregateType.value());
                if(asyncEventHandlerMapper.containsKey(aggregateType.value())) {
                    asyncEventHandlerMapper.get(aggregateType.value()).add(eventHandler);
                } else {
                    List<AsyncEventHandler> eventHandlerList = new ArrayList<>();
                    eventHandlerList.add(eventHandler);
                    asyncEventHandlerMapper.put(aggregateType.value(), eventHandlerList);
                }
            }
        });
    }

    public static void loadSyncEventHandlers(List<SyncEventHandler> eventHandlers) {
        eventHandlers.forEach(eventHandler -> {
            AggregateType aggregateType = AnnotationUtils.findAnnotation(eventHandler.getClass(), AggregateType.class);
            if (aggregateType != null) {
                if(syncEventHandlerMapper.containsKey(aggregateType.value())) {
                    syncEventHandlerMapper.get(aggregateType.value()).add(eventHandler);
                } else {
                    List<SyncEventHandler> eventHandlerList = new ArrayList<>();
                    eventHandlerList.add(eventHandler);
                    syncEventHandlerMapper.put(aggregateType.value(), eventHandlerList);
                }
            }
        });
    }
}
