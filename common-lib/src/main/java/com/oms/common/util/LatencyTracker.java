package com.oms.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

/**
 * Nano-precision latency tracker for tick-to-trade measurement.
 */
public class LatencyTracker {
    private static final Logger log = LoggerFactory.getLogger(LatencyTracker.class);

    private final LongAdder totalNanos = new LongAdder();
    private final LongAdder count      = new LongAdder();

    public long startNano() {
        return System.nanoTime();
    }

    public void record(long startNano) {
        totalNanos.add(System.nanoTime() - startNano);
        count.increment();
    }

    public double avgMicros() {
        long c = count.sum();
        return c == 0 ? 0 : totalNanos.sum() / (c * 1_000.0);
    }

    public double avgMillis() {
        return avgMicros() / 1_000.0;
    }

    public long totalCount() {
        return count.sum();
    }

    public void logStats(int every) {
        long c = count.sum();
        if (c > 0 && c % every == 0) {
            log.info("[LATENCY] Avg tick-to-trade: {} µs over {} orders",
                    String.format("%.2f", avgMicros()), c);
        }
    }

    public void reset() {
        totalNanos.reset();
        count.reset();
    }
}
