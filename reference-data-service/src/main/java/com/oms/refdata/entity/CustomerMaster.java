package com.oms.refdata.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "customer_master", indexes = {
    @Index(name = "idx_account_type", columnList = "account_type")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerMaster {

    @Id
    @Column(name = "customer_id", length = 32)
    private String customerId;

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "account_type", nullable = false, length = 32)
    private String accountType;  // RETAIL, INSTITUTIONAL, PROP

    @Column(name = "max_order_qty", precision = 18, scale = 4)
    private BigDecimal maxOrderQty;

    @Column(name = "max_order_value", precision = 18, scale = 2)
    private BigDecimal maxOrderValue;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
