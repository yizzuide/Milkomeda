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

package com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.processor;

import com.github.yizzuide.milkomeda.molecule.MoleculeContext;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.EventSourcingProperties;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.AsyncEventHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service.EventSubscriptionProcessor;
import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGNotification;
import org.postgresql.jdbc.PgConnection;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.*;

/**
 * This processor is used to subscribe to Postgres channel events.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 17:09
 */
@RequiredArgsConstructor
@Slf4j
public class PostgresChannelEventSubscriptionProcessor {
    private static final String REDIS_POSTGRES_CHANNEL_EVENT_LOCK_KEY = "milkomeda:molecule:%s:lock-pg-chanel-event-sub";

    private final List<AsyncEventHandler> eventHandlers;
    private final EventSubscriptionProcessor eventSubscriptionProcessor;
    private final EventSourcingProperties properties;

    private volatile PgConnection connection;
    private final ExecutorService executor = newExecutor();
    private CountDownLatch latch = new CountDownLatch(0);
    private Future<?> future = CompletableFuture.completedFuture(null);

    private static ExecutorService newExecutor() {
        CustomizableThreadFactory threadFactory =
                new CustomizableThreadFactory("postgres-channel-event-subscription-");
        threadFactory.setDaemon(true);
        return Executors.newSingleThreadExecutor(threadFactory);
    }

    @PostConstruct
    public synchronized void start() {
        if (this.latch.getCount() > 0) {
            return;
        }
        this.latch = new CountDownLatch(1);
        this.future = executor.submit(() -> {
            // 同一服务多实例，使用分布式锁（防止异步事件重复处理）
            String lockKey = String.format(REDIS_POSTGRES_CHANNEL_EVENT_LOCK_KEY, properties.getServiceName());
            StringRedisTemplate redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
            redisTemplate.opsForValue().setIfAbsent(lockKey, "1");
            try {
                while (isActive()) {
                    try {
                        PgConnection conn = getPgConnection();
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute("LISTEN channel_event_notify");
                        } catch (Exception ex) {
                            try {
                                conn.close();
                            } catch (Exception suppressed) {
                                ex.addSuppressed(suppressed);
                            }
                            throw ex;
                        }

                        // handle old events on startup
                        this.eventHandlers.forEach(this::processNewEvents);

                        try {
                            this.connection = conn;
                            while (isActive()) {
                                PGNotification[] notifications = conn.getNotifications(0);
                                // Unfortunately, there is no good way of interrupting a notification
                                // poll but by closing its connection.
                                if (!isActive()) {
                                    return;
                                }
                                if (notifications != null) {
                                    for (PGNotification notification : notifications) {
                                        String parameter = notification.getParameter();
                                        MoleculeContext.getAsyncEventHandlers(parameter).forEach(this::processNewEvents);
                                    }
                                }
                                // delay handle changed events
                                try {
                                    PulsarHolder.getPulsar().delay(() -> this.eventHandlers.forEach(this::processNewEvents),  properties.getHandleChangedEventsDelayTime().toMillis());
                                } catch (Exception e) {
                                    log.error("Error while processing changed events asynchronously", e);
                                }
                            }
                        } finally {
                            conn.close();
                        }
                    } catch (Exception e) {
                        // The getNotifications method does not throw a meaningful message on interruption.
                        // Therefore, we do not log an error, unless it occurred while active.
                        if (isActive()) {
                            log.error("Failed to poll notifications from Postgres database", e);
                        }
                    }
                }
            } finally {
                this.latch.countDown();
                // 分布式锁释放
                redisTemplate.delete(lockKey);
            }
        });
    }

    private PgConnection getPgConnection() throws SQLException {
        BindResult<DataSourceProperties> bindResult = Binder.get(ApplicationContextHolder.getPendingConfigurableEnvironment()).bind(properties.getDatasourcePrefix(), DataSourceProperties.class);
        DataSourceProperties props = bindResult.get();
        return DriverManager.getConnection(
                props.determineUrl(),
                props.determineUsername(),
                props.determinePassword()
        ).unwrap(PgConnection.class);
    }

    private boolean isActive() {
        if (Thread.interrupted()) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    private void processNewEvents(AsyncEventHandler eventHandler) {
        try {
            eventSubscriptionProcessor.processNewEvents(eventHandler);
        } catch (Exception e) {
            log.warn("Failed to handle new events for subscription %s"
                    .formatted(eventHandler.getSubscriptionName()), e);
        }
    }

    @PreDestroy
    public synchronized void stop() {
        String lockKey = String.format(REDIS_POSTGRES_CHANNEL_EVENT_LOCK_KEY, properties.getServiceName());
        StringRedisTemplate redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
        redisTemplate.delete(lockKey);
        if (this.future.isDone()) {
            return;
        }
        this.future.cancel(true);
        PgConnection conn = this.connection;
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
        try {
            if (!this.latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Failed to stop %s".formatted(PostgresChannelEventSubscriptionProcessor.class.getName()));
            }
        } catch (InterruptedException ignored) {
        }
    }
}