package com.oms.audit.repository;

import com.oms.audit.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByEntityTypeAndEntityId(String entityType, String entityId);

    Page<AuditLogEntity> findByEventType(String eventType, Pageable pageable);

    List<AuditLogEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to);
}
