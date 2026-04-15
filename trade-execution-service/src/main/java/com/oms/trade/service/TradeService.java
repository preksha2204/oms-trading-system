package com.oms.trade.service;

import com.oms.common.dto.TradeDTO;
import com.oms.common.kafka.KafkaTopics;
import com.oms.trade.entity.TradeEntity;
import com.oms.trade.repository.TradeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    private static final int BATCH_SIZE  = 1000;
    private static final int QUEUE_LIMIT = 500_000;

    private final TradeRepository              tradeRepository;
    private final IdempotencyService           idempotencyService;
    private final KafkaTemplate<String, TradeDTO> kafkaTemplate;

    // Async persistence queue — handles 2M trades
    private final LinkedBlockingQueue<TradeDTO> persistQueue = new LinkedBlockingQueue<>(QUEUE_LIMIT);

    @PostConstruct
    public void startPersistenceWorker() {
        Thread worker = new Thread(this::persistenceLoop, "trade-persist-worker");
        worker.setDaemon(true);
        worker.start();
        log.info("Trade persistence worker started (capacity={})", QUEUE_LIMIT);
    }

    public void processTrade(TradeDTO trade) {
        if (idempotencyService.isDuplicate(trade)) {
            log.warn("Skipping duplicate trade: id={}", trade.getTradeId());
            return;
        }
        idempotencyService.register(trade.getIdempotencyKey());

        // Enqueue for async DB write (non-blocking)
        if (!persistQueue.offer(trade)) {
            log.warn("Trade persistence queue full — tradeId={}", trade.getTradeId());
        }

        // Publish persisted event for downstream listeners
        kafkaTemplate.send(KafkaTopics.TRADE_PERSISTED, trade.getSymbol(), trade);
        log.info("Trade processed: id={} symbol={} qty={} price={}",
                trade.getTradeId(), trade.getSymbol(), trade.getQuantity(), trade.getPrice());
    }

    private void persistenceLoop() {
        List<TradeDTO> batch = new ArrayList<>(BATCH_SIZE);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TradeDTO first = persistQueue.poll(100, TimeUnit.MILLISECONDS);
                if (first != null) {
                    batch.add(first);
                    persistQueue.drainTo(batch, BATCH_SIZE - 1);
                    List<TradeEntity> entities = batch.stream().map(this::toEntity).toList();
                    tradeRepository.saveAll(entities);
                    log.debug("Persisted trade batch of {}", entities.size());
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Trade persistence error", e);
                batch.clear();
            }
        }
    }

    public Page<TradeDTO> getTradesBySymbol(String symbol, int page, int size) {
        return tradeRepository.findBySymbol(symbol,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "executedAt")))
                .map(this::toDTO);
    }

    public List<TradeDTO> getTradesBySymbolAndTime(String symbol, Instant from, Instant to) {
        return tradeRepository.findBySymbolAndTimeRange(symbol, from, to)
                .stream().map(this::toDTO).toList();
    }

    private TradeEntity toEntity(TradeDTO t) {
        return TradeEntity.builder()
                .tradeId(t.getTradeId())
                .orderId(t.getBuyOrderId())
                .clOrdId(t.getBuyClOrdId() != null ? t.getBuyClOrdId() : "UNKNOWN")
                .symbol(t.getSymbol())
                .side(1)
                .quantity(t.getQuantity())
                .price(t.getPrice())
                .execId(t.getExecId() != null ? t.getExecId() : "")
                .idempotencyKey(t.getIdempotencyKey())
                .executedAt(t.getExecutedAt() != null ? t.getExecutedAt() : Instant.now())
                .build();
    }

    private TradeDTO toDTO(TradeEntity e) {
        return TradeDTO.builder()
                .tradeId(e.getTradeId())
                .buyOrderId(e.getOrderId())
                .symbol(e.getSymbol())
                .quantity(e.getQuantity())
                .price(e.getPrice())
                .execId(e.getExecId())
                .executedAt(e.getExecutedAt())
                .build();
    }

    public int queueSize() { return persistQueue.size(); }
}
