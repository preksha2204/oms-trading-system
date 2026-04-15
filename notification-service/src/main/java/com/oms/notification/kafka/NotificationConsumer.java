package com.oms.notification.kafka;

import com.oms.common.dto.ExecutionReportDTO;
import com.oms.common.dto.OrderDTO;
import com.oms.common.dto.TradeDTO;
import com.oms.common.kafka.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "notification-service")
    public void onOrderCreated(OrderDTO order) {
        log.info("[NOTIFY] New order: clOrdId={} symbol={} side={} qty={} price={}",
                order.getClOrdId(), order.getSymbol(), order.getSide(),
                order.getQuantity(), order.getPrice());
    }

    @KafkaListener(topics = KafkaTopics.ORDER_UPDATED, groupId = "notification-service")
    public void onOrderUpdated(OrderDTO order) {
        log.info("[NOTIFY] Order updated: clOrdId={} status={} filledQty={}",
                order.getClOrdId(), order.getStatus(), order.getFilledQty());
    }

    @KafkaListener(topics = KafkaTopics.TRADE_EXECUTED, groupId = "notification-service")
    public void onTradeExecuted(TradeDTO trade) {
        log.info("[NOTIFY] Trade executed: tradeId={} symbol={} qty={} price={}",
                trade.getTradeId(), trade.getSymbol(), trade.getQuantity(), trade.getPrice());
    }

    @KafkaListener(topics = KafkaTopics.EXEC_REPORT, groupId = "notification-service")
    public void onExecReport(ExecutionReportDTO report) {
        log.info("[NOTIFY] Execution report: clOrdId={} execType={} status={}",
                report.getClOrdId(), report.getExecType(), report.getOrdStatus());
    }
}
