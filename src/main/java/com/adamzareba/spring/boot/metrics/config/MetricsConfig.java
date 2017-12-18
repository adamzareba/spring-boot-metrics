package com.adamzareba.spring.boot.metrics.config;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.readytalk.metrics.StatsDReporter;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import static com.readytalk.metrics.StatsDReporter.Builder;

@Configuration
@EnableMetrics
public class MetricsConfig extends MetricsConfigurerAdapter {

    private static final String PROP_METRIC_REG_JVM_MEMORY = "jvm.memory";
    private static final String PROP_METRIC_REG_JVM_THREADS = "jvm.threads";
    private static final String PROP_METRIC_REG_JVM_GARBAGE = "jvm.gc";
    private static final String PROP_METRIC_REG_JVM_CLASSES = "jvm.classes";

    @Value("${statsD.host}")
    private String host;

    @Value("${statsD.port}")
    private int port;

    @Value("${statsD.period}")
    private int period;

    @Value("${statsD.prefix}")
    private String prefix;

    @Override
    public void configureReporters(MetricRegistry metricRegistry) {
        StatsDReporter reporter = getStatsDReporterBuilder(metricRegistry).build(host, port);
        registerReporter(reporter);
        reporter.start(period, TimeUnit.SECONDS);
    }

    private Builder getStatsDReporterBuilder(MetricRegistry metricRegistry) {
        metricRegistry.register(PROP_METRIC_REG_JVM_MEMORY, new MemoryUsageGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_THREADS, new ThreadStatesGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_GARBAGE, new GarbageCollectorMetricSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_CLASSES, new ClassLoadingGaugeSet());

        return StatsDReporter.forRegistry(metricRegistry)
                .prefixedWith(prefix)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL);
    }
}