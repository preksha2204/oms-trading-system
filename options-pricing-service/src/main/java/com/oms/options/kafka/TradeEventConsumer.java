package com.oms.options.kafka;

import com.oms.common.dto.OptionPriceDTO;
import com.oms.common.dto.TradeDTO;
import com.oms.common.kafka.KafkaTopics;
import com.oms.options.pricing.BlackScholesCalculator;
import com.oms.options.pricing.BlackScholesInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeEventConsumer {

    private final BlackScholesCalculator        calculator;
    private final KafkaTemplate<String, OptionPriceDTO> kafkaTemplate;

    // In-memory option config: underlying → list of options
    private final ConcurrentHashMap<String, List<Map<String, Object>>> optionConfig =
            new ConcurrentHashMap<>();

    @KafkaListener(topics = KafkaTopics.TRADE_EXECUTED, groupId = "options-pricing")
    public void onTradeExecuted(TradeDTO trade) {
        String symbol = trade.getSymbol();
        double underlyingPrice = trade.getPrice().doubleValue();

        // Recalculate AAPL example options — in production, loaded from reference-data-service
        List<Map<String, Object>> options = getOptionsForUnderlying(symbol);
        options.parallelStream().forEach(opt -> {
            BlackScholesInput input = BlackScholesInput.builder()
                    .symbol((String) opt.get("symbol"))
                    .underlyingPrice(underlyingPrice)
                    .strikePrice((double) opt.get("strike"))
                    .volatility((double) opt.get("volatility"))
                    .riskFreeRate(0.05)
                    .timeToExpiry((double) opt.get("tte"))
                    .build();

            OptionPriceDTO price = calculator.calculate(input);
            price.setUnderlying(symbol);
            kafkaTemplate.send(KafkaTopics.PRICING_UPDATE, price.getSymbol(), price);
            log.debug("Option repriced: {} call={} put={}", price.getSymbol(), price.getCallPrice(), price.getPutPrice());
        });
    }

    /** Stub option chain — replace with call to reference-data-service in production */
    private List<Map<String, Object>> getOptionsForUnderlying(String underlying) {
        return switch (underlying) {
            case "AAPL" -> List.of(
                Map.of("symbol","AAPL240621C00150000","strike",150.0,"volatility",0.28,"tte",0.42),
                Map.of("symbol","AAPL240621P00145000","strike",145.0,"volatility",0.30,"tte",0.42)
            );
            case "MSFT" -> List.of(
                Map.of("symbol","MSFT240621C00310000","strike",310.0,"volatility",0.25,"tte",0.42)
            );
            default -> List.of();
        };
    }
}
