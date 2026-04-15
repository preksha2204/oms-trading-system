package com.oms.order.service;

import com.oms.common.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderValidationService {

    private final RestTemplate restTemplate;

    public void validate(OrderDTO order) {
        if (order.getQuantity() == null || order.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Quantity must be > 0");

        if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Price must be > 0");

        if (order.getSymbol() == null || order.getSymbol().isBlank())
            throw new IllegalArgumentException("Symbol is mandatory");

        // Validate symbol against Reference Data Service
        try {
            Boolean valid = restTemplate.getForObject(
                    "http://reference-data-service:8087/api/ref-data/symbols/validate?symbol=" + order.getSymbol(),
                    Boolean.class);
            if (Boolean.FALSE.equals(valid)) {
                throw new IllegalArgumentException("Invalid symbol: " + order.getSymbol());
            }
        } catch (Exception e) {
            log.warn("Could not validate symbol with ref-data service: {}", e.getMessage());
            // Fail-open: continue without symbol validation if ref-data is unreachable
        }
        log.debug("Order validation passed: clOrdId={}", order.getClOrdId());
    }
}
