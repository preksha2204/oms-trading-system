package com.oms.ws.broadcaster;

import com.oms.common.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastOrder(OrderDTO order) {
        messagingTemplate.convertAndSend("/topic/orders", order);
        log.debug("Broadcast order: {} status={}", order.getOrderId(), order.getStatus());
    }

    public void broadcastTrade(TradeDTO trade) {
        messagingTemplate.convertAndSend("/topic/trades", trade);
        messagingTemplate.convertAndSend("/topic/trades/" + trade.getSymbol(), trade);
        log.debug("Broadcast trade: {} symbol={}", trade.getTradeId(), trade.getSymbol());
    }

    public void broadcastMarketData(MarketDataEvent event) {
        messagingTemplate.convertAndSend("/topic/market-data", event);
        messagingTemplate.convertAndSend("/topic/market-data/" + event.getSymbol(), event);
    }

    public void broadcastOptionPrice(OptionPriceDTO price) {
        messagingTemplate.convertAndSend("/topic/options", price);
        messagingTemplate.convertAndSend("/topic/options/" + price.getSymbol(), price);
        log.debug("Broadcast option price: {} call={}", price.getSymbol(), price.getCallPrice());
    }

    public void broadcastExecReport(ExecutionReportDTO report) {
        messagingTemplate.convertAndSend("/topic/executions", report);
        messagingTemplate.convertAndSend("/topic/executions/" + report.getClOrdId(), report);
    }
}
