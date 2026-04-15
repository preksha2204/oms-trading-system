package com.oms.refdata.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "security_master", indexes = {
    @Index(name = "idx_underlying", columnList = "underlying_symbol")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SecurityMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "security_id")
    private Long securityId;

    @Column(name = "symbol", nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(name = "isin", length = 12)
    private String isin;

    @Column(name = "cusip", length = 9)
    private String cusip;

    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "exchange", nullable = false, length = 20)
    private String exchange;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "lot_size", precision = 18, scale = 4)
    private BigDecimal lotSize;

    @Column(name = "tick_size", precision = 18, scale = 6)
    private BigDecimal tickSize;

    @Column(name = "min_price", precision = 18, scale = 6)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 18, scale = 6)
    private BigDecimal maxPrice;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "option_type", length = 1)
    private String optionType;  // C=Call, P=Put, null=equity

    @Column(name = "strike_price", precision = 18, scale = 6)
    private BigDecimal strikePrice;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "underlying_symbol", length = 20)
    private String underlyingSymbol;

    @Column(name = "volatility", precision = 10, scale = 6)
    private BigDecimal volatility;
}
