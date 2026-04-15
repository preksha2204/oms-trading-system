package com.oms.audit.service;

import com.oms.audit.entity.AuditLogEntity;
import com.oms.audit.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final int BATCH_SIZE  = 500;
    private static final int QUEUE_LIMIT = 200_000;

    private final AuditLogRepository auditRepo;
    private final ObjectMapper       objectMapper;
    private final LinkedBlockingQueue<AuditLogEntity> auditQueue = new LinkedBlockingQueue<>(QUEUE_LIMIT);

    @PostConstruct
    public void startWorker() {
        Thread worker = new Thread(this::persistLoop, "audit-persist-worker");
        worker.setDaemon(true);
        worker.start();
        log.info("Audit persistence worker started");
    }

    public void audit(String entityType, String entityId, String eventType, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            AuditLogEntity entry = AuditLogEntity.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .eventType(eventType)
                    .payload(json)
                    .createdAt(Instant.now())
                    .build();
            if (!auditQueue.offer(entry)) {
                log.warn("Audit queue full — dropping event: {}/{}", entityType, entityId);
            }
        } catch (Exception e) {
            log.error("Failed to serialize audit event", e);
        }
    }

    private void persistLoop() {
        List<AuditLogEntity> batch = new ArrayList<>(BATCH_SIZE);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                AuditLogEntity first = auditQueue.poll(100, TimeUnit.MILLISECONDS);
                if (first != null) {
                    batch.add(first);
                    auditQueue.drainTo(batch, BATCH_SIZE - 1);
                    auditRepo.saveAll(batch);
                    log.debug("Audit batch persisted: {} entries", batch.size());
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Audit persistence error", e);
                batch.clear();
            }
        }
    }
}
