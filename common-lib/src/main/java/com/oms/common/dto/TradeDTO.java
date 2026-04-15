package com.oms.common.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String      tradeId;
    private Long        buyOrderId;
    private Long        sellOrderId;
    private String      buyClOrdId;
    private String      sellClOrdId;
    private String      symbol;
    private BigDecimal  quantity;
    private BigDecimal  price;
    private String      execId;
    private String      idempotencyKey;
    private Instant     executedAt;
}
