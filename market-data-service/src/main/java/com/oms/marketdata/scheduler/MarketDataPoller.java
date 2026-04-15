package com.oms.marketdata.scheduler;

import com.oms.common.dto.MarketDataEvent;
import com.oms.marketdata.kafka.MarketDataProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Market Data CSV Poller — reads quotes.csv on schedule and emits delta updates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataPoller {

    private final MarketDataProducer producer;
    // Snapshot of last known values — only emit on change (delta update)
    private final Map<String, MarketDataEvent> lastSnapshot = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "${marketdata.poll-interval-ms:1000}")
    public void poll() {
        List<MarketDataEvent> current = parseCsv();
        int emitted = 0;
        for (MarketDataEvent event : current) {
            MarketDataEvent prev = lastSnapshot.get(event.getSymbol());
            if (!event.equals(prev)) {
                lastSnapshot.put(event.getSymbol(), event);
                producer.publish(event);
                emitted++;
            }
        }
        if (emitted > 0) {
            log.debug("Market data poll: {} deltas emitted", emitted);
        }
    }

    private List<MarketDataEvent> parseCsv() {
        List<MarketDataEvent> events = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("market-data/quotes.csv")))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; } // skip header
                String[] cols = line.split(",");
                if (cols.length < 7) continue;
                events.add(MarketDataEvent.builder()
                        .symbol(cols[0].trim())
                        .bid(new BigDecimal(cols[1].trim()))
                        .ask(new BigDecimal(cols[2].trim()))
                        .last(new BigDecimal(cols[3].trim()))
                        .open(new BigDecimal(cols[4].trim()))
                        .high(new BigDecimal(cols[5].trim()))
                        .low(new BigDecimal(cols[6].trim()))
                        .volume(cols.length > 7 ? Long.parseLong(cols[7].trim()) : 0L)
                        .timestamp(Instant.now())
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to parse market data CSV", e);
        }
        return events;
    }
}
