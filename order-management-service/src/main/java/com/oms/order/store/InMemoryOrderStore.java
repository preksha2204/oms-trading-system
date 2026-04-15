package com.oms.order.store;

import com.oms.common.dto.OrderDTO;
import com.oms.common.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory order store using ConcurrentHashMap for O(1) access.
 * Primary hot path for order lookup during matching — no DB round-trip.
 */
@Slf4j
@Component
public class InMemoryOrderStore {

    private final ConcurrentHashMap<Long, OrderDTO>   byOrderId  = new ConcurrentHashMap<>(100_000);
    private final ConcurrentHashMap<String, OrderDTO> byClOrdId  = new ConcurrentHashMap<>(100_000);

    public void put(OrderDTO order) {
        byOrderId.put(order.getOrderId(), order);
        byClOrdId.put(order.getClOrdId(), order);
    }

    public OrderDTO getByOrderId(Long orderId) {
        return byOrderId.get(orderId);
    }

    public OrderDTO getByClOrdId(String clOrdId) {
        return byClOrdId.get(clOrdId);
    }

    public void update(OrderDTO order) {
        byOrderId.put(order.getOrderId(), order);
        byClOrdId.put(order.getClOrdId(), order);
    }

    public void remove(Long orderId) {
        OrderDTO o = byOrderId.remove(orderId);
        if (o != null) byClOrdId.remove(o.getClOrdId());
    }

    public Collection<OrderDTO> allOrders() {
        return byOrderId.values();
    }

    public long countActive() {
        return byOrderId.values().stream()
                .filter(o -> o.getStatus() == OrderStatus.NEW || o.getStatus() == OrderStatus.PARTIAL)
                .count();
    }

    public int size() {
        return byOrderId.size();
    }
}
