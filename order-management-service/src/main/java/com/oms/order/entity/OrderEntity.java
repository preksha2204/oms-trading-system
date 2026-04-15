package com.oms.order.entity;

import com.oms.common.enums.OrderSide;
import com.oms.common.enums.OrderStatus;
import com.oms.common.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_symbol_status", columnList = "symbol,status"),
    @Index(name = "idx_received_at",   columnList = "received_at"),
    @Index(name = "idx_customer",      columnList = "customer_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    private Long id;

    @Column(name = "cl_ord_id", nullable = false, unique = true, length = 64)
    private String clOrdId;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "customer_id", length = 32)
    private String customerId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "side", nullable = false)
    private OrderSide side;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "filled_qty", precision = 18, scale = 4)
    private BigDecimal filledQty;

    @Column(name = "price", nullable = false, precision = 18, scale = 6)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "fix_session_id", length = 128)
    private String fixSessionId;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
