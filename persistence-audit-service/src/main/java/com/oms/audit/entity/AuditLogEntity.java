package com.oms.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_created", columnList = "created_at")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "entity_type", nullable = false, length = 32)
    private String entityType;  // ORDER, TRADE, EXECUTION

    @Column(name = "entity_id", nullable = false, length = 64)
    private String entityId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;   // ORDER_CREATED, TRADE_EXECUTED, etc.

    @Column(name = "payload", nullable = false, columnDefinition = "JSON")
    private String payload;     // JSON serialized event

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
