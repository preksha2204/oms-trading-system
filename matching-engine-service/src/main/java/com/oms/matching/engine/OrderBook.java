package com.oms.matching.engine;

import com.oms.common.dto.OrderDTO;
import com.oms.common.enums.OrderSide;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Two-sided order book for one symbol.
 * Bids:  ConcurrentSkipListMap (descending)  — highest price first
 * Asks:  ConcurrentSkipListMap (ascending)   — lowest  price first
 */
@Getter
public class OrderBook {

    private final String symbol;

    // Bids: descending (best bid at top)
    private final ConcurrentSkipListMap<BigDecimal, PriceLevel> bids =
            new ConcurrentSkipListMap<>(Comparator.reverseOrder());

    // Asks: ascending (best ask at top)
    private final ConcurrentSkipListMap<BigDecimal, PriceLevel> asks =
            new ConcurrentSkipListMap<>();

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public void addOrder(OrderDTO order) {
        ConcurrentSkipListMap<BigDecimal, PriceLevel> book = sideBook(order.getSide());
        book.computeIfAbsent(order.getPrice(), PriceLevel::new).addOrder(order);
    }

    public void removeOrder(OrderDTO order) {
        ConcurrentSkipListMap<BigDecimal, PriceLevel> book = sideBook(order.getSide());
        PriceLevel level = book.get(order.getPrice());
        if (level != null) {
            level.removeOrder(order.getOrderId());
            if (level.isEmpty()) book.remove(order.getPrice());
        }
    }

    public BigDecimal bestBid() {
        return bids.isEmpty() ? null : bids.firstKey();
    }

    public BigDecimal bestAsk() {
        return asks.isEmpty() ? null : asks.firstKey();
    }

    public int bidLevels() { return bids.size(); }
    public int askLevels() { return asks.size(); }

    private ConcurrentSkipListMap<BigDecimal, PriceLevel> sideBook(OrderSide side) {
        return side == OrderSide.BUY ? bids : asks;
    }
}
