package com.oms.matching.kafka;

import com.oms.common.dto.OrderDTO;
import com.oms.common.kafka.KafkaTopics;
import com.oms.matching.engine.MatchingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final MatchingEngine matchingEngine;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED,
                   groupId = "matching-engine",
                   concurrency = "3")
    public void onOrderCreated(OrderDTO order) {
        log.debug("Matching engine received order: clOrdId={} symbol={}", 
                order.getClOrdId(), order.getSymbol());
        matchingEngine.match(order);
    }
}
