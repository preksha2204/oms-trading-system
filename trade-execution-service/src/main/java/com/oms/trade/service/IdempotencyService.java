package com.oms.trade.service;

import com.oms.common.dto.TradeDTO;
import com.oms.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Idempotency guard — prevents duplicate trade booking.
 * Uses both an in-memory set (fast path) and DB check (persistent path).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final TradeRepository tradeRepository;
    // Fast in-memory dedup cache (survives within a JVM session)
    private final ConcurrentHashMap<String, Boolean> recentKeys = new ConcurrentHashMap<>(50_000);

    public boolean isDuplicate(TradeDTO trade) {
        String key = trade.getIdempotencyKey();
        if (key == null) return false;

        // Fast check: in-memory
        if (recentKeys.containsKey(key)) {
            log.warn("Duplicate trade detected (in-memory): key={}", key);
            return true;
        }

        // Persistent check: DB
        if (tradeRepository.existsByIdempotencyKey(key)) {
            recentKeys.put(key, Boolean.TRUE);
            log.warn("Duplicate trade detected (DB): key={}", key);
            return true;
        }
        return false;
    }

    public void register(String idempotencyKey) {
        if (idempotencyKey != null) {
            recentKeys.put(idempotencyKey, Boolean.TRUE);
        }
        // Evict old entries if cache grows too large
        if (recentKeys.size() > 200_000) {
            recentKeys.clear();
            log.info("Idempotency cache cleared (size exceeded 200K)");
        }
    }
}
