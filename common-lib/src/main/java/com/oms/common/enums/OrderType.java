package com.oms.common.enums;

public enum OrderType {
    MARKET('1'), LIMIT('2'), STOP('3'), STOP_LIMIT('4');

    private final char fixValue;

    OrderType(char fixValue) { this.fixValue = fixValue; }

    public char getFixValue() { return fixValue; }

    public static OrderType fromFix(char c) {
        for (OrderType t : values()) if (t.fixValue == c) return t;
        throw new IllegalArgumentException("Unknown FIX order type: " + c);
    }
}
