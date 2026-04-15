package com.oms.order.queue;

import com.oms.common.dto.OrderDTO;
import com.oms.order.entity.OrderEntity;
import com.oms.order.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Producer-Consumer persistence worker.
 * FIX callback adds to queue (non-blocking) → worker drains and batch-inserts to DB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPersistenceWorker {

    private static final int BATCH_SIZE  = 500;
    private static final int QUEUE_LIMIT = 100_000;

    private final LinkedBlockingQueue<OrderDTO> queue = new LinkedBlockingQueue<>(QUEUE_LIMIT);
    private final OrderRepository orderRepository;

    @PostConstruct
    public void start() {
        Thread worker = new Thread(this::run, "order-persist-worker");
        worker.setDaemon(true);
        worker.start();
        log.info("OrderPersistenceWorker started (queue capacity={})", QUEUE_LIMIT);
    }

    /** Non-blocking enqueue — called from hot FIX path */
    public boolean enqueue(OrderDTO order) {
        boolean offered = queue.offer(order);
        if (!offered) {
            log.warn("Persistence queue FULL — ClOrdID={} dropped", order.getClOrdId());
        }
        return offered;
    }

    public int queueSize() {
        return queue.size();
    }

    private void run() {
        List<OrderDTO> batch = new ArrayList<>(BATCH_SIZE);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                OrderDTO first = queue.poll(100, TimeUnit.MILLISECONDS);
                if (first != null) {
                    batch.add(first);
                    queue.drainTo(batch, BATCH_SIZE - 1);
                    List<OrderEntity> entities = batch.stream().map(this::toEntity).toList();
                    orderRepository.saveAll(entities);
                    log.debug("Persisted batch of {} orders", entities.size());
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("OrderPersistenceWorker interrupted — shutting down");
            } catch (Exception e) {
                log.error("Persistence worker error", e);
                batch.clear(); // drop bad batch — could route to DLQ
            }
        }
    }

    private OrderEntity toEntity(OrderDTO o) {
        return OrderEntity.builder()
                .id(o.getOrderId())
                .clOrdId(o.getClOrdId())
                .symbol(o.getSymbol())
                .customerId(o.getCustomerId())
                .side(o.getSide())
                .orderType(o.getOrderType())
                .quantity(o.getQuantity())
                .filledQty(o.getFilledQty())
                .price(o.getPrice())
                .status(o.getStatus())
                .fixSessionId(o.getFixSessionId())
                .receivedAt(o.getReceivedAt() != null ? o.getReceivedAt() : Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
