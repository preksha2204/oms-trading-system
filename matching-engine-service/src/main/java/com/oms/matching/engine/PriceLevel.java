package com.oms.matching.engine;

import com.oms.common.dto.OrderDTO;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * A single price level in the order book.
 * Orders are maintained in a LinkedList preserving FIFO time priority.
 */
@Getter
public class PriceLevel {

    private final BigDecimal price;
    private final LinkedList<OrderDTO> orders = new LinkedList<>();

    public PriceLevel(BigDecimal price) {
        this.price = price;
    }

    public void addOrder(OrderDTO order) {
        orders.addLast(order);    // Add to tail — preserves time priority (FIFO)
    }

    public OrderDTO peek() {
        return orders.peekFirst();
    }

    public OrderDTO poll() {
        return orders.pollFirst(); // Remove from head — oldest order matched first
    }

    public void removeOrder(Long orderId) {
        orders.removeIf(o -> o.getOrderId().equals(orderId));
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    public int depth() {
        return orders.size();
    }

    public BigDecimal totalQty() {
        return orders.stream()
                .map(o -> o.getQuantity().subtract(o.getFilledQty()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
