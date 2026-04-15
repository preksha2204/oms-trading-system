package com.oms.fix.kafka;

import com.oms.common.dto.OrderDTO;
import com.oms.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixOrderProducer {

    private final KafkaTemplate<String, OrderDTO> kafkaTemplate;

    public void publishOrder(OrderDTO order) {
        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, order.getSymbol(), order)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order to Kafka: ClOrdID={}", order.getClOrdId(), ex);
                    } else {
                        log.debug("Order published: ClOrdID={} partition={}",
                                order.getClOrdId(),
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
