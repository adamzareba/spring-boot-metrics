package com.adamzareba.spring.boot.metrics.config;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.readytalk.metrics.StatsDReporter;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableMetrics
public class MetricsConfig extends MetricsConfigurerAdapter {

    @Value("${statsD.host}")
    public String hostIP;

    @Value("${statsD.port}")
    public int port;

    @Value("${statsD.period}")
    public int period;

    @Value("${statsD.prefix}")
    public String prefix;

    @Override
    public void configureReporters(MetricRegistry metricRegistry) {
        StatsDReporter reporter = getStatsDReporterBuilder(metricRegistry).build(hostIP, port);
        registerReporter(reporter);
        reporter.start(period, TimeUnit.SECONDS);
    }

    private StatsDReporter.Builder getStatsDReporterBuilder(MetricRegistry metricRegistry) {
        metricRegistry.register("gc", new GarbageCollectorMetricSet());
        metricRegistry.register("memory", new MemoryUsageGaugeSet());
        metricRegistry.register("threads", new ThreadStatesGaugeSet());
        return StatsDReporter.forRegistry(metricRegistry)
                .prefixedWith(prefix)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL);
    }
}