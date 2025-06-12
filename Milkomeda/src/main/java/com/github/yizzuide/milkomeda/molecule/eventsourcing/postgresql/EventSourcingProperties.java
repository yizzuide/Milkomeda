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
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

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

    public record Snapshotting(
            boolean enabled,
            @Min(DEFAULT_NTH_EVENT)
            int nthEvent
    ) {
    }

    @Data
    static class PollingSubscription {
        private String initialDelay = "PT1S";
        private String interval = "PT1S";
    }
}