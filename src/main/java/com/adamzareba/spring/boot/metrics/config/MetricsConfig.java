package com.adamzareba.spring.boot.metrics.config;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpointMetricReader;
import org.springframework.boot.actuate.metrics.writer.GaugeWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MetricsConfig {

    @Autowired
    private InfluxDB influxDB;

    @Value("${spring.influx.database}")
    private String database;

    @Bean
    @ExportMetricWriter
    public GaugeWriter influxMetricsWriter() {
        influxDB.setDatabase(database);
        influxDB.enableBatch(10, 1000, TimeUnit.MILLISECONDS);

        return (metricValue) -> {
            Point point = Point.measurement(metricValue.getName()).time(metricValue.getTimestamp().getTime(), TimeUnit.MILLISECONDS)
                    .addField("metricValue", metricValue.getValue()).build();
            influxDB.write(point);
        };
    }

    @Bean
    public MetricsEndpointMetricReader metricsEndpointMetricReader(final MetricsEndpoint metricsEndpoint) {
        return new MetricsEndpointMetricReader(metricsEndpoint);
    }
}
