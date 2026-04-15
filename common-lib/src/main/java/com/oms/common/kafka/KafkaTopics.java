package com.oms.common.kafka;

public final class KafkaTopics {
    private KafkaTopics() {}

    // Order lifecycle
    public static final String ORDER_CREATED   = "order.created";
    public static final String ORDER_UPDATED   = "order.updated";
    public static final String ORDER_CANCELLED = "order.cancelled";

    // Trade lifecycle
    public static final String TRADE_EXECUTED  = "trade.executed";
    public static final String TRADE_PERSISTED = "trade.persisted";

    // Market data
    public static final String MARKET_DATA     = "market.data";

    // Options pricing
    public static final String PRICING_UPDATE  = "pricing.update";

    // Audit
    public static final String AUDIT_EVENT     = "audit.event";

    // Execution reports
    public static final String EXEC_REPORT     = "exec.report";
}
