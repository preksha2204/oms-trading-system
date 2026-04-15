package com.oms.fix.validation;

import com.oms.common.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class FixMessageValidator {

    public void validate(OrderDTO order) {
        if (order.getClOrdId() == null || order.getClOrdId().isBlank()) {
            throw new IllegalArgumentException("ClOrdID (Tag 11) is mandatory");
        }
        if (order.getSymbol() == null || order.getSymbol().isBlank()) {
            throw new IllegalArgumentException("Symbol (Tag 55) is mandatory");
        }
        if (order.getSide() == null) {
            throw new IllegalArgumentException("Side (Tag 54) is mandatory");
        }
        if (order.getQuantity() == null || order.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("OrderQty (Tag 38) must be > 0");
        }
        if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price (Tag 44) must be > 0");
        }
        log.debug("Order validation passed for ClOrdID={}", order.getClOrdId());
    }
}
