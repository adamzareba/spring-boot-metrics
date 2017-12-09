package com.adamzareba.spring.boot.metrics.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.springframework.boot.actuate.autoconfigure.ExportMetricReader;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.reader.MetricReader;
import org.springframework.boot.actuate.metrics.reader.MetricRegistryMetricReader;
import org.springframework.boot.actuate.metrics.writer.GaugeWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MetricsConfig {

    @Bean
    @ExportMetricReader
    public MetricReader metricReader() {
        return new MetricRegistryMetricReader(metricRegistry());
    }
//
//    @Bean
//    @ExportMetricWriter
//    GaugeWriter influxMetricsWriter() {
//        InfluxDB influxDB = InfluxDBFactory.connect( "http://localhost:8086",
//                "root",
//                "root");
//        String dbName = "myMetricsDB";	// the name of the datastore you choose
//        influxDB.createDatabase( dbName);
//        InfluxDBMetricWriter.Builder builder = new InfluxDBMetricWriter.Builder( influxDB);
//        builder.databaseName( dbName);
//        builder.batchActions( 500);	// number of points for batch before data is sent to Influx
//        return builder.build();
//    }

    public MetricRegistry metricRegistry() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
        metricRegistry.register("jvm.mem", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm.thread-states", new ThreadStatesGaugeSet());


        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
        String dbName = "aTimeSeries";
        influxDB.createDatabase(dbName);
        String rpName = "aRetentionPolicy";
        influxDB.createRetentionPolicy(rpName, dbName, "30d", "30m", 2, true);

        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .tag("async", "true")
                .retentionPolicy(rpName)
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        Point point1 = Point.measurement("cpu")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("idle", 190L)
                .addField("user", 19L)
                .addField("system", 11L)
                .build();
        Point point2 = Point.measurement("disk")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("used", 180L)
                .addField("free", 2L)
                .build();
        batchPoints.point(point1);
        batchPoints.point(point2);
        influxDB.write(batchPoints);
//        Query query = new Query("SELECT idle FROM cpu", dbName);
//        influxDB.query(query);
//        influxDB.dropRetentionPolicy(rpName, dbName);
//        influxDB.deleteDatabase(dbName);

        return metricRegistry;
    }
}