package com.oms.common.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketDataEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String      symbol;
    private BigDecimal  bid;
    private BigDecimal  ask;
    private BigDecimal  last;
    private BigDecimal  open;
    private BigDecimal  high;
    private BigDecimal  low;
    private Long        volume;
    private Instant     timestamp;
}
