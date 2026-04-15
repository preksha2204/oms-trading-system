package com.oms.common.util;

/**
 * Distributed Snowflake ID Generator.
 * Layout: 41 bits timestamp | 10 bits nodeId | 12 bits sequence
 * Epoch: 2024-01-01T00:00:00Z
 */
public class SnowflakeIdGenerator {
    private static final long EPOCH      = 1704067200000L; // 2024-01-01
    private static final long NODE_BITS  = 10L;
    private static final long SEQ_BITS   = 12L;
    private static final long MAX_NODE   = ~(-1L << NODE_BITS);
    private static final long MAX_SEQ    = ~(-1L << SEQ_BITS);
    private static final long NODE_SHIFT = SEQ_BITS;
    private static final long TIME_SHIFT = NODE_BITS + SEQ_BITS;

    private final long nodeId;
    private long lastTimestamp = -1L;
    private long sequence      = 0L;

    public SnowflakeIdGenerator(long nodeId) {
        if (nodeId < 0 || nodeId > MAX_NODE) {
            throw new IllegalArgumentException("nodeId must be between 0 and " + MAX_NODE);
        }
        this.nodeId = nodeId;
    }

    public synchronized long nextId() {
        long now = System.currentTimeMillis();
        if (now < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards. Refusing to generate ID.");
        }
        if (now == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQ;
            if (sequence == 0) {
                now = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = now;
        return ((now - EPOCH) << TIME_SHIFT) | (nodeId << NODE_SHIFT) | sequence;
    }

    private long waitNextMillis(long lastTs) {
        long ts = System.currentTimeMillis();
        while (ts <= lastTs) ts = System.currentTimeMillis();
        return ts;
    }

    /** Extract timestamp from a Snowflake ID */
    public static long extractTimestamp(long id) {
        return (id >> TIME_SHIFT) + EPOCH;
    }
}
