package com.oms.common.enums;

public enum ExecType {
    NEW('0'), PARTIAL_FILL('1'), FILL('2'), DONE_FOR_DAY('3'),
    CANCELLED('4'), REPLACE('5'), PENDING_CANCEL('6'),
    STOPPED('7'), REJECTED('8'), SUSPENDED('9'), TRADE('F');

    private final char fixValue;

    ExecType(char fixValue) { this.fixValue = fixValue; }

    public char getFixValue() { return fixValue; }
}
