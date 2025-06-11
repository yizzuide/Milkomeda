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
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.Aggregate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Transactional(propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class AggregateRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public void createAggregateIfAbsent(@NonNull String aggregateType,
                                        @NonNull UUID aggregateId) {
        jdbcTemplate.update("""
                        INSERT INTO ES_AGGREGATE (ID, VERSION, AGGREGATE_TYPE)
                        VALUES (:aggregateId, 0, :aggregateType)
                        ON CONFLICT DO NOTHING
                        """,
                Map.of(
                        "aggregateId", aggregateId,
                        "aggregateType", aggregateType
                ));
    }

    public boolean checkAndUpdateAggregateVersion(@NonNull UUID aggregateId,
                                                  int expectedVersion,
                                                  int newVersion) {
        int updatedRows = jdbcTemplate.update("""
                        UPDATE ES_AGGREGATE
                           SET VERSION = :newVersion
                         WHERE ID = :aggregateId
                           AND VERSION = :expectedVersion
                        """,
                Map.of(
                        "newVersion", newVersion,
                        "aggregateId", aggregateId,
                        "expectedVersion", expectedVersion
                ));
        return updatedRows > 0;
    }

    @SneakyThrows
    public void createAggregateSnapshot(@NonNull Aggregate aggregate) {
        jdbcTemplate.update("""
                        INSERT INTO ES_AGGREGATE_SNAPSHOT (AGGREGATE_ID, VERSION, JSON_DATA)
                        VALUES (:aggregateId, :version, :jsonObj::json)
                        """,
                Map.of(
                        "aggregateId", aggregate.getAggregateId(),
                        "version", aggregate.getVersion(),
                        "jsonObj", objectMapper.writeValueAsString(aggregate)
                ));
    }

    public Optional<Aggregate> readAggregateSnapshot(@NonNull UUID aggregateId,
                                                     @Nullable Integer version) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("aggregateId", aggregateId);
        parameters.addValue("version", version, Types.INTEGER);

        return jdbcTemplate.query("""
                        SELECT a.AGGREGATE_TYPE,
                               s.JSON_DATA
                          FROM ES_AGGREGATE_SNAPSHOT s
                          JOIN ES_AGGREGATE a ON a.ID = s.AGGREGATE_ID
                         WHERE s.AGGREGATE_ID = :aggregateId
                           AND (:version IS NULL OR s.VERSION <= :version)
                         ORDER BY s.VERSION DESC
                         LIMIT 1
                        """,
                parameters,
                this::toAggregate
        ).stream().findFirst();
    }

    @SneakyThrows
    private Aggregate toAggregate(ResultSet rs, int rowNum) {
        String aggregateType = rs.getString("AGGREGATE_TYPE");
        PGobject jsonObj = (PGobject) rs.getObject("JSON_DATA");
        String json = jsonObj.getValue();
        Class<? extends Aggregate> aggregateClass = MoleculeContext.getClassByAggregateType(aggregateType);
        return objectMapper.readValue(json, aggregateClass);
    }
}