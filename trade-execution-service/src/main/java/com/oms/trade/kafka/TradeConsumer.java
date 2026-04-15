package com.oms.trade.kafka;

import com.oms.common.dto.TradeDTO;
import com.oms.common.kafka.KafkaTopics;
import com.oms.trade.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeConsumer {

    private final TradeService tradeService;

    @KafkaListener(topics = KafkaTopics.TRADE_EXECUTED,
                   groupId = "trade-execution",
                   concurrency = "3")
    public void onTradeExecuted(TradeDTO trade) {
        log.debug("Trade received: id={} symbol={}", trade.getTradeId(), trade.getSymbol());
        tradeService.processTrade(trade);
    }
}
