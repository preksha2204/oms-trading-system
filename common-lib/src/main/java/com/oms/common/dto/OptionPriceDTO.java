package com.oms.common.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionPriceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String      symbol;
    private String      underlying;
    private BigDecimal  strikePrice;
    private String      optionType;        // C or P
    private double      callPrice;
    private double      putPrice;
    private double      delta;
    private double      gamma;
    private double      vega;
    private double      theta;
    private double      underlyingPrice;
    private double      volatility;
    private double      riskFreeRate;
    private double      timeToExpiry;
    private Instant     calculatedAt;
}
