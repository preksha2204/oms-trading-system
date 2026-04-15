package com.oms.matching.kafka;

import com.oms.common.dto.TradeDTO;
import com.oms.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeProducer {

    private final KafkaTemplate<String, TradeDTO> kafkaTemplate;

    public void publish(TradeDTO trade) {
        kafkaTemplate.send(KafkaTopics.TRADE_EXECUTED, trade.getSymbol(), trade)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish trade: tradeId={}", trade.getTradeId(), ex);
                    } else {
                        log.debug("Trade published: tradeId={} symbol={} qty={} price={}",
                                trade.getTradeId(), trade.getSymbol(),
                                trade.getQuantity(), trade.getPrice());
                    }
                });
    }
}
