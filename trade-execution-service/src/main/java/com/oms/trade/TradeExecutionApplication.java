package com.oms.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class TradeExecutionApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradeExecutionApplication.class, args);
    }
}
