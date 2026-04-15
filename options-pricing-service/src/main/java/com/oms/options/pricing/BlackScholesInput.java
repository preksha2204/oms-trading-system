package com.oms.options.pricing;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BlackScholesInput {
    private String symbol;
    private double underlyingPrice;
    private double strikePrice;
    private double riskFreeRate;
    private double volatility;
    private double timeToExpiry;   // in years
}
