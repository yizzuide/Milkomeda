package com.github.yizzuide.milkomeda.demo;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerHttpObservationDocumentation;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationConvention;

/**
 * MvcObservationConfiguration
 *
 * @author yizzuide
 * Create at 2025/06/08 23:24
 */
@Configuration
public class MvcObservationConfiguration {
    @Bean
    public CustomServerRequestObservationConvention customServerRequestObservationConvention() {
        return new CustomServerRequestObservationConvention();
    }

    @Bean
    public ServerRequestObservationFilter serverRequestObservationFilter() {
        return new ServerRequestObservationFilter();
    }



    // Spring Boot 3.0: As a result of the integration with the Observation support, we are now deprecating the previous instrumentation.
    //  The filters, interceptors performing the actual instrumentation have been removed entirely. For example, the WebMvcMetricsFilter
    //  has been deleted entirely and is effectively replaced by Spring Framework’s ServerHttpObservationFilter. On the client side,
    //  the MetricsRestTemplateCustomizer has been removed and a `ObservationRestTemplateCustomizer` is applied instead on the RestTemplateBuilder bean.
    //  In our new model, both tag providers and contributors are replaced by observation conventions.
    public static class CustomServerRequestObservationConvention implements ServerRequestObservationConvention {

        @Override
        public String getName() {
            // will be used for the metric name
            return "http.server.requests";
        }

        @Override
        public String getContextualName(ServerRequestObservationContext context) {
            // will be used for the trace name
            return "http " + context.getCarrier().getMethod().toLowerCase();
        }

        @NotNull
        @Override
        public KeyValues getLowCardinalityKeyValues(@NotNull ServerRequestObservationContext context) {
            return KeyValues.of(method(context), status(context), exception(context));
        }

        @NotNull
        @Override
        public KeyValues getHighCardinalityKeyValues(@NotNull ServerRequestObservationContext context) {
            return KeyValues.of(httpUrl(context));
        }

        protected KeyValue method(ServerRequestObservationContext context) {
            // You should reuse as much as possible the corresponding ObservationDocumentation for key names
            return KeyValue.of(ServerHttpObservationDocumentation.LowCardinalityKeyNames.METHOD, context.getCarrier().getMethod());
        }

        protected KeyValue status(ServerRequestObservationContext context) {
            return KeyValue.of(ServerHttpObservationDocumentation.LowCardinalityKeyNames.STATUS, context.getResponse() == null ? "" : context.getResponse().getStatus() + "");
        }

        protected KeyValue exception(ServerRequestObservationContext context) {
            return KeyValue.of(ServerHttpObservationDocumentation.LowCardinalityKeyNames.EXCEPTION, context.getError() == null ? "" : context.getError().getMessage());
        }

        protected KeyValue httpUrl(ServerRequestObservationContext context) {
            return KeyValue.of(ServerHttpObservationDocumentation.HighCardinalityKeyNames.HTTP_URL, context.getCarrier().getRequestURI());
        }
    }

    // You can be also similar goals using a custom ObservationFilter - adding or removing key values for an observation.
    //  Filters do not replace the default convention and are used as a post-processing component.
    public static class ServerRequestObservationFilter implements ObservationFilter {
        @NotNull
        @Override
        public Observation.Context map(@NotNull Observation.Context context) {
            if (context instanceof ServerRequestObservationContext serverContext) {
                context.addLowCardinalityKeyValue(KeyValue.of("project", "spring"));
                String customAttribute = String.valueOf(serverContext.getCarrier().getAttribute("customAttribute"));
                context.addLowCardinalityKeyValue(KeyValue.of("custom.attribute", customAttribute));
            }
            return context;
        }
    }

    // Micrometer’s JvmInfoMetrics is now autoconfigured.

    // The new ObservationRegistry interface can be used to create observations which provide a single API for both metrics and traces.
    //  Spring Boot now autoconfigures an instance of ObservationRegistry for you.
    // If micrometer-core is on the classpath, a DefaultMeterObservationHandler is registered on the ObservationRegistry,
    //  which means that every stopped Observation leads to a timer. ObservationPredicate, GlobalObservationConvention and
    //  ObservationHandler are automatically registered on the ObservationRegistry. You can use ObservationRegistryCustomizer to further customize the `ObservationRegistry` if you need to.

    // We have moved the properties controlling the actuator metrics export. The old schema was management.metrics.export.<product>,
    //  the new one is management.<product>.metrics.export (Example: the prometheus properties moved from management.metrics.export.prometheus to management.prometheus.metrics.export).
    //  If you are using the spring-boot-properties-migrator, you will get notified at startup.

    // Spring Boot now autoconfigures Micrometer Tracing for you. This includes support for Brave, OpenTelemetry, Zipkin and Wavefront.
    //  When using the Micrometer Observation API, finishing observations will lead to spans reported to Zipkin or Wavefront.
    //  Tracing can be controlled with properties under management.tracing. Zipkin can be configured with management.zipkin.tracing, while Wavefront uses management.wavefront.

    // An OtlpMeterRegistry is now autoconfigured when io.micrometer:micrometer-registry-otlp is on the classpath. The meter registry can be configured using management.otlp.metrics.export.* properties.

    // When there is a Micrometer Tracing `Tracer` bean and Prometheus is on the classpath, a SpanContextSupplier is now autoconfigured. This supplier links metrics to traces by making the current trace ID and span ID available to Prometheus.
    // The Push Gateway can be configured to perform a `PUT` on shutdown. To do so, set `management.prometheus.metrics.export.pushgateway.shutdown-operation` to `put`. Additionally, the existing `push` setting has been deprecated and `post` should now be used instead.

    // The HealthIndicator for MongoDB now supports MongoDB’s Stable API. The buildInfo query has been replaced with
    //  `isMaster` and the response now contains `maxWireVersion` instead of `version`.
}
