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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Event-sourcing properties.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/10 16:46
 */
@Data
@Validated
@ConfigurationProperties(prefix = EventSourcingProperties.PREFIX)
public class EventSourcingProperties {

    public static final String PREFIX = "milkomeda.molecule.event-sourcing";

    public static final String DEFAULT_DATASOURCE_PREFIX = "spring.datasource";

    public static final int DEFAULT_NTH_EVENT = 2;

    private static final Snapshotting NO_SNAPSHOTTING = new Snapshotting(false, DEFAULT_NTH_EVENT);

    /**
     * Enable event sourcing.
     */
    private Boolean enabled = false;

    /**
     * Set service name when used {@link SubscriptionType#POSTGRES_CHANNEL}.
     */
    private String serviceName;

    /**
     * DataSource prefix.
     */
    private String datasourcePrefix = DEFAULT_DATASOURCE_PREFIX;

    /**
     * Aggregate package.
     */
    @NotEmpty
    private String aggregatePackage;

    /**
     * Event package.
     */
    @NotEmpty
    private String eventPackage;

    /**
     * Event Sourcing snapshotting category.
     */
    @Valid
    private Map<String, Snapshotting> snapshotting = new HashMap<>();

    /**
     * Enable sync read model before transaction commit (must set true if application service invoke in transactional).
     */
    private Boolean syncReadModelBeforeTransactionCommit = true;

    /**
     * Set delay time is a compensation mechanism for handle changed events when handle new events had broken with throws exception.
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration handleChangedEventsDelayTime = Duration.ofMillis(5000);

    /**
     * Async event subscription type.
     */
    private SubscriptionType subscriptionType = SubscriptionType.POSTGRES_CHANNEL;

    /**
     * Subscription config for {@link SubscriptionType#POLLING}.
     */
    private PollingSubscription pollingSubscription = new PollingSubscription();

    public Snapshotting getSnapshotting(String aggregateType) {
        return snapshotting.getOrDefault(aggregateType, NO_SNAPSHOTTING);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Snapshotting {
        /**
         * Enable snapshotting.
         */
        private boolean enabled;

        /**
         * Save a snapshot after nth events
         */
        @Min(DEFAULT_NTH_EVENT)
        private int nthEvent = DEFAULT_NTH_EVENT;
    }

    @Data
    public static class PollingSubscription {
        private String initialDelay = "PT1S";
        private String interval = "PT1S";
    }
}