package com.oms.order.kafka;

import com.oms.common.dto.OrderDTO;
import com.oms.common.kafka.KafkaTopics;
import com.oms.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "order-management")
    public void onOrderCreated(OrderDTO order) {
        log.debug("OMS received order from Kafka: clOrdId={}", order.getClOrdId());
        // If order came via REST (not FIX gateway), process it
        if (order.getOrderId() == null) {
            orderService.processOrder(order);
        }
    }
}
