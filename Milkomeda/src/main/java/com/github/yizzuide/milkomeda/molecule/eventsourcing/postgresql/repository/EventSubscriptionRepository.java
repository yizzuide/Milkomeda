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

import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventSubscriptionCheckpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@Transactional(propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class EventSubscriptionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void createSubscriptionIfAbsent(String subscriptionName) {
        jdbcTemplate.update("""
                        INSERT INTO ES_EVENT_SUBSCRIPTION (SUBSCRIPTION_NAME, LAST_TRANSACTION_ID, LAST_EVENT_ID)
                        VALUES (:subscriptionName, '0'::xid8, 0)
                        ON CONFLICT DO NOTHING
                        """,
                Map.of("subscriptionName", subscriptionName)
        );
    }

    public Optional<EventSubscriptionCheckpoint> readCheckpointAndLockSubscription(String subscriptionName) {
        return jdbcTemplate.query("""
                        SELECT LAST_TRANSACTION_ID::text,
                               LAST_EVENT_ID
                          FROM ES_EVENT_SUBSCRIPTION
                         WHERE SUBSCRIPTION_NAME = :subscriptionName
                           FOR UPDATE SKIP LOCKED
                        """,
                Map.of("subscriptionName", subscriptionName),
                this::toEventSubscriptionCheckpoint
        ).stream().findFirst();
    }

    public boolean updateEventSubscription(String subscriptionName,
                                           BigInteger lastProcessedTransactionId,
                                           long lastProcessedEventId) {
        int updatedRows = jdbcTemplate.update("""
                        UPDATE ES_EVENT_SUBSCRIPTION
                           SET LAST_TRANSACTION_ID = :lastProcessedTransactionId::xid8,
                               LAST_EVENT_ID = :lastProcessedEventId
                         WHERE SUBSCRIPTION_NAME = :subscriptionName
                        """,
                Map.of(
                        "subscriptionName", subscriptionName,
                        "lastProcessedTransactionId", lastProcessedTransactionId.toString(),
                        "lastProcessedEventId", lastProcessedEventId
                ));
        return updatedRows > 0;
    }

    private EventSubscriptionCheckpoint toEventSubscriptionCheckpoint(ResultSet rs, int rowNum) throws SQLException {
        String lastProcessedTransactionId = rs.getString("LAST_TRANSACTION_ID");
        long lastProcessedEventId = rs.getLong("LAST_EVENT_ID");
        return new EventSubscriptionCheckpoint(new BigInteger(lastProcessedTransactionId), lastProcessedEventId);
    }
}