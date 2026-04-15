package com.oms.order.service;

import com.oms.common.dto.ExecutionReportDTO;
import com.oms.common.dto.OrderDTO;
import com.oms.common.enums.ExecType;
import com.oms.common.enums.OrderStatus;
import com.oms.common.kafka.KafkaTopics;
import com.oms.common.util.SnowflakeIdGenerator;
import com.oms.order.queue.OrderPersistenceWorker;
import com.oms.order.store.InMemoryOrderStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
public class OrderService {

    private final InMemoryOrderStore      orderStore;
    private final OrderPersistenceWorker  persistenceWorker;
    private final OrderValidationService  validationService;
    private final OrderEnrichmentService  enrichmentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SnowflakeIdGenerator    idGenerator;

    public OrderService(InMemoryOrderStore orderStore,
                        OrderPersistenceWorker persistenceWorker,
                        OrderValidationService validationService,
                        OrderEnrichmentService enrichmentService,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderStore        = orderStore;
        this.persistenceWorker = persistenceWorker;
        this.validationService = validationService;
        this.enrichmentService = enrichmentService;
        this.kafkaTemplate     = kafkaTemplate;
        this.idGenerator       = new SnowflakeIdGenerator(1L);
    }

    public OrderDTO processOrder(OrderDTO order) {
        // 1. Assign unique Snowflake ID
        order.setOrderId(idGenerator.nextId());
        order.setReceivedAt(Instant.now());
        order.setFilledQty(BigDecimal.ZERO);
        order.setStatus(OrderStatus.NEW);

        // 2. Validate
        validationService.validate(order);

        // 3. Enrich from reference data
        enrichmentService.enrich(order);

        // 4. Store in memory (fast path)
        orderStore.put(order);

        // 5. Async persist to DB (non-blocking)
        persistenceWorker.enqueue(order);

        // 6. Publish to Kafka for matching engine
        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, order.getSymbol(), order);

        // 7. Publish ExecutionReport NEW
        publishExecReport(order, ExecType.NEW, order.getQuantity(), BigDecimal.ZERO, order.getPrice());

        log.info("Order processed: id={} clOrdId={} symbol={} side={} qty={} price={}",
                order.getOrderId(), order.getClOrdId(),
                order.getSymbol(), order.getSide(),
                order.getQuantity(), order.getPrice());

        return order;
    }

    public OrderDTO updateFromTrade(Long orderId, BigDecimal filledQty, BigDecimal tradePrice) {
        OrderDTO order = orderStore.getByOrderId(orderId);
        if (order == null) {
            log.warn("Order not found in memory: {}", orderId);
            return null;
        }
        order.setFilledQty(order.getFilledQty().add(filledQty));
        boolean fullyFilled = order.getFilledQty().compareTo(order.getQuantity()) >= 0;
        order.setStatus(fullyFilled ? OrderStatus.FILLED : OrderStatus.PARTIAL);
        orderStore.update(order);
        persistenceWorker.enqueue(order);

        ExecType execType = fullyFilled ? ExecType.FILL : ExecType.PARTIAL_FILL;
        BigDecimal leavesQty = order.getQuantity().subtract(order.getFilledQty()).max(BigDecimal.ZERO);
        publishExecReport(order, execType, filledQty, order.getFilledQty(), tradePrice);

        kafkaTemplate.send(KafkaTopics.ORDER_UPDATED, order.getSymbol(), order);
        return order;
    }

    private void publishExecReport(OrderDTO order, ExecType execType,
                                   BigDecimal lastQty, BigDecimal cumQty, BigDecimal lastPx) {
        BigDecimal leavesQty = order.getQuantity().subtract(cumQty).max(BigDecimal.ZERO);
        ExecutionReportDTO report = ExecutionReportDTO.builder()
                .orderId(String.valueOf(order.getOrderId()))
                .clOrdId(order.getClOrdId())
                .execId("E-" + System.nanoTime())
                .execType(execType)
                .ordStatus(order.getStatus())
                .symbol(order.getSymbol())
                .side(order.getSide())
                .lastQty(lastQty)
                .lastPx(lastPx)
                .cumQty(cumQty)
                .leavesQty(leavesQty)
                .avgPx(lastPx)
                .fixSessionId(order.getFixSessionId())
                .transactTime(Instant.now())
                .build();
        kafkaTemplate.send(KafkaTopics.EXEC_REPORT, order.getClOrdId(), report);
    }

    public OrderDTO getByOrderId(Long orderId) {
        return orderStore.getByOrderId(orderId);
    }

    public OrderDTO getByClOrdId(String clOrdId) {
        return orderStore.getByClOrdId(clOrdId);
    }
}
