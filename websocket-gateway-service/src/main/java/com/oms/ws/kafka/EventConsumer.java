package com.oms.ws.kafka;

import com.oms.common.dto.*;
import com.oms.common.kafka.KafkaTopics;
import com.oms.ws.broadcaster.EventBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final EventBroadcaster broadcaster;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "ws-gateway")
    public void onOrderCreated(OrderDTO order) {
        broadcaster.broadcastOrder(order);
    }

    @KafkaListener(topics = KafkaTopics.ORDER_UPDATED, groupId = "ws-gateway")
    public void onOrderUpdated(OrderDTO order) {
        broadcaster.broadcastOrder(order);
    }

    @KafkaListener(topics = KafkaTopics.TRADE_EXECUTED, groupId = "ws-gateway")
    public void onTradeExecuted(TradeDTO trade) {
        broadcaster.broadcastTrade(trade);
    }

    @KafkaListener(topics = KafkaTopics.MARKET_DATA, groupId = "ws-gateway")
    public void onMarketData(MarketDataEvent event) {
        broadcaster.broadcastMarketData(event);
    }

    @KafkaListener(topics = KafkaTopics.PRICING_UPDATE, groupId = "ws-gateway")
    public void onPricingUpdate(OptionPriceDTO price) {
        broadcaster.broadcastOptionPrice(price);
    }

    @KafkaListener(topics = KafkaTopics.EXEC_REPORT, groupId = "ws-gateway")
    public void onExecReport(ExecutionReportDTO report) {
        broadcaster.broadcastExecReport(report);
    }
}
