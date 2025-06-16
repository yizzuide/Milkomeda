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
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * This repository is used to store the {@link Aggregate} and snapshot with version.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 18:25
 */
@Transactional(propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class AggregateRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public Long createAggregateIfAbsent(@NonNull String aggregateType,
                                        @Nullable Long aggregateId) {
        if (aggregateId == null) {
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(ApplicationContextHolder.get().getBean(JdbcTemplate.class))
                    .withTableName("es_aggregate")
                    .usingGeneratedKeyColumns("id");
            Map<String, ? extends Serializable> params = Map.of("version", 0, "aggregate_type", aggregateType, "created_at", Timestamp.from(Instant.now()));
            Number key = simpleJdbcInsert.executeAndReturnKey(params);
            return key.longValue();
        }

        jdbcTemplate.update("""
                        INSERT INTO ES_AGGREGATE (ID, VERSION, AGGREGATE_TYPE)
                        VALUES (:aggregateId, 0, :aggregateType)
                        ON CONFLICT DO NOTHING
                        """,
                Map.of(
                        "aggregateId", aggregateId,
                        "aggregateType", aggregateType
                ));
        return null;
    }

    public void deleteById(@NonNull Long aggregateId) {
        jdbcTemplate.update("""
                        DELETE FROM ES_AGGREGATE WHERE ID = :aggregateId
                        """,
                Map.of(
                        "aggregateId", aggregateId
                ));
    }

    public boolean checkAndUpdateAggregateVersion(@NonNull Long aggregateId,
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
                        INSERT INTO ES_AGGREGATE_SNAPSHOT (AGGREGATE_ID, VERSION, AGGREGATE_TYPE, JSON_DATA)
                        VALUES (:aggregateId, :version, :aggregateType, :jsonObj::json)
                        """,
                Map.of(
                        "aggregateId", aggregate.getAggregateId(),
                        "version", aggregate.getVersion(),
                        "aggregateType", MoleculeContext.getAggregateTypeByClass(aggregate.getClass()),
                        "jsonObj", objectMapper.writeValueAsString(aggregate)
                ));
    }

    public Optional<Aggregate> readAggregateSnapshot(@NonNull Long aggregateId,
                                                     @Nullable Integer version) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("aggregateId", aggregateId);
        parameters.addValue("version", version, Types.INTEGER);

        return jdbcTemplate.query("""
                        SELECT AGGREGATE_TYPE,
                               JSON_DATA
                          FROM ES_AGGREGATE_SNAPSHOT
                         WHERE (:version IS NULL OR VERSION <= :version)
                         ORDER BY VERSION DESC
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