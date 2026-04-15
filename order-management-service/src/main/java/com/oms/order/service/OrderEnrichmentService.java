package com.oms.order.service;

import com.oms.common.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderEnrichmentService {

    public void enrich(OrderDTO order) {
        // Defaults if not set
        if (order.getCurrency() == null || order.getCurrency().isBlank()) {
            order.setCurrency("USD");
        }
        if (order.getExchange() == null || order.getExchange().isBlank()) {
            order.setExchange("NYSE");
        }
        if (order.getCustomerId() == null || order.getCustomerId().isBlank()) {
            order.setCustomerId("DEFAULT_CUSTOMER");
        }
        log.debug("Order enriched: clOrdId={} currency={} exchange={}",
                order.getClOrdId(), order.getCurrency(), order.getExchange());
    }
}
