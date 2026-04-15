package com.oms.marketdata.kafka;

import com.oms.common.dto.MarketDataEvent;
import com.oms.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataProducer {

    private final KafkaTemplate<String, MarketDataEvent> kafkaTemplate;

    public void publish(MarketDataEvent event) {
        kafkaTemplate.send(KafkaTopics.MARKET_DATA, event.getSymbol(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish market data: {}", event.getSymbol(), ex);
                });
    }
}
