package com.oms.common.enums;

public enum OrderSide {
    BUY('1'), SELL('2');

    private final char fixValue;

    OrderSide(char fixValue) { this.fixValue = fixValue; }

    public char getFixValue() { return fixValue; }

    public static OrderSide fromFix(char c) {
        for (OrderSide s : values()) if (s.fixValue == c) return s;
        throw new IllegalArgumentException("Unknown FIX side: " + c);
    }
}
