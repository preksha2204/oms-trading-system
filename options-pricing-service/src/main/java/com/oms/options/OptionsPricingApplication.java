package com.oms.options;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class OptionsPricingApplication {
    public static void main(String[] args) {
        SpringApplication.run(OptionsPricingApplication.class, args);
    }
}
