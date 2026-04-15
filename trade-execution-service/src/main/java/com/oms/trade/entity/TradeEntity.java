package com.oms.trade.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trades", indexes = {
    @Index(name = "idx_symbol_time", columnList = "symbol,executed_at"),
    @Index(name = "idx_order_id",    columnList = "order_id")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TradeEntity {

    @Id
    @Column(name = "trade_id", length = 40)
    private String tradeId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "cl_ord_id", nullable = false, length = 64)
    private String clOrdId;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "side", nullable = false)
    private Integer side;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "price", nullable = false, precision = 18, scale = 6)
    private BigDecimal price;

    @Column(name = "exec_id", nullable = false, length = 64)
    private String execId;

    @Column(name = "idempotency_key", nullable = false, length = 128, unique = true)
    private String idempotencyKey;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;
}
