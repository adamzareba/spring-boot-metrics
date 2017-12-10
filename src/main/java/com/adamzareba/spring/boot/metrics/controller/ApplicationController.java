package com.adamzareba.spring.boot.metrics.controller;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class ApplicationController {

    @Autowired
    private InfluxDB influxDB;

    @Value("${spring.influx.database}")
    private String database;

    @RequestMapping("/")
    @ResponseBody
    public String home() {
        double duration = 10.0 + Math.random() * (100.0 - 10.0);

        if (!influxDB.databaseExists(database)) {
            influxDB.createDatabase(database);
        }
        BatchPoints batchPoints = BatchPoints.database(database).build();
        Point randomRequestTime = Point.measurement("home-response")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("requestDuration", duration)
                .build();
        batchPoints.point(randomRequestTime);
        influxDB.write(batchPoints);

        return "Hello World!";
    }
}