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

package com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yizzuide.milkomeda.molecule.MoleculeContext;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventWithId;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * This repository is used to store the {@link Event} with version.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 18:46
 */
@Transactional(propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class EventRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public <T extends Event> EventWithId<T> appendEvent(@NonNull Event event) {
        List<EventWithId<T>> result = jdbcTemplate.query("""
                        INSERT INTO ES_EVENT (TRANSACTION_ID, AGGREGATE_ID, VERSION, AGGREGATE_TYPE, EVENT_TYPE, JSON_DATA)
                        VALUES(pg_current_xact_id(), :aggregateId, :version, :aggregateType, :eventType, :jsonObj::json)
                        RETURNING ID, TRANSACTION_ID::text, EVENT_TYPE, JSON_DATA
                        """,
                Map.of(
                        "aggregateId", event.getAggregateId(),
                        "version", event.getVersion(),
                        "aggregateType", event.getAggregateType(),
                        "eventType", MoleculeContext.getEventTypeByClass(event.getClass()),
                        "jsonObj", objectMapper.writeValueAsString(event)
                ),
                this::toEvent);
        return result.getFirst();
    }

    public List<EventWithId<Event>> readEvents(@NonNull Long aggregateId,
                                               @Nullable Integer fromVersion,
                                               @Nullable Integer toVersion) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("aggregateId", aggregateId);
        parameters.addValue("fromVersion", fromVersion, Types.INTEGER);
        parameters.addValue("toVersion", toVersion, Types.INTEGER);

        return jdbcTemplate.query("""
                        SELECT ID,
                               TRANSACTION_ID::text,
                               EVENT_TYPE,
                               JSON_DATA
                          FROM ES_EVENT
                         WHERE AGGREGATE_ID = :aggregateId
                           AND (:fromVersion IS NULL OR VERSION > :fromVersion)
                           AND (:toVersion IS NULL OR VERSION <= :toVersion)
                         ORDER BY VERSION ASC
                        """,
                parameters,
                this::toEvent);
    }

    public List<EventWithId<Event>> readEventsAfterCheckpoint(@NonNull String aggregateType,
                                                              @NonNull BigInteger lastProcessedTransactionId,
                                                              long lastProcessedEventId) {
        return jdbcTemplate.query("""
                        SELECT ID,
                               TRANSACTION_ID::text,
                               EVENT_TYPE,
                               JSON_DATA
                          FROM ES_EVENT
                         WHERE AGGREGATE_TYPE = :aggregateType
                           AND (TRANSACTION_ID, ID) > (:lastProcessedTransactionId::xid8, :lastProcessedEventId)
                           AND TRANSACTION_ID < pg_snapshot_xmin(pg_current_snapshot())
                         ORDER BY TRANSACTION_ID ASC, ID ASC
                        """,
                Map.of(
                        "aggregateType", aggregateType,
                        "lastProcessedTransactionId", lastProcessedTransactionId.toString(),
                        "lastProcessedEventId", lastProcessedEventId
                ),
                this::toEvent);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private <T extends Event> EventWithId<T> toEvent(ResultSet rs, int rowNum) {
        long id = rs.getLong("ID");
        String transactionId = rs.getString("TRANSACTION_ID");
        String eventType = rs.getString("EVENT_TYPE");
        PGobject jsonObj = (PGobject) rs.getObject("JSON_DATA");
        String json = jsonObj.getValue();
        Class<? extends Event> eventClass = MoleculeContext.getClassByEventType(eventType);
        Event event = objectMapper.readValue(json, eventClass);
        return new EventWithId<>(id, new BigInteger(transactionId), (T) event);
    }
}