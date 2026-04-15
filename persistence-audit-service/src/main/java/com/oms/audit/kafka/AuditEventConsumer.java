package com.oms.audit.kafka;

import com.oms.common.dto.OrderDTO;
import com.oms.common.dto.TradeDTO;
import com.oms.common.kafka.KafkaTopics;
import com.oms.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditService auditService;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "audit-service")
    public void onOrderCreated(OrderDTO order) {
        auditService.audit("ORDER", String.valueOf(order.getOrderId()), "ORDER_CREATED", order);
    }

    @KafkaListener(topics = KafkaTopics.ORDER_UPDATED, groupId = "audit-service")
    public void onOrderUpdated(OrderDTO order) {
        auditService.audit("ORDER", String.valueOf(order.getOrderId()), "ORDER_UPDATED", order);
    }

    @KafkaListener(topics = KafkaTopics.TRADE_EXECUTED, groupId = "audit-service")
    public void onTradeExecuted(TradeDTO trade) {
        auditService.audit("TRADE", trade.getTradeId(), "TRADE_EXECUTED", trade);
    }

    @KafkaListener(topics = KafkaTopics.TRADE_PERSISTED, groupId = "audit-service")
    public void onTradePersisted(TradeDTO trade) {
        auditService.audit("TRADE", trade.getTradeId(), "TRADE_PERSISTED", trade);
    }
}
