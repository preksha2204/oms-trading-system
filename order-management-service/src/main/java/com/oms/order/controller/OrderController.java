package com.oms.order.controller;

import com.oms.common.dto.OrderDTO;
import com.oms.order.service.OrderService;
import com.oms.order.store.InMemoryOrderStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService      orderService;
    private final InMemoryOrderStore orderStore;

    @PostMapping
    public ResponseEntity<OrderDTO> submitOrder(@Valid @RequestBody OrderDTO order) {
        OrderDTO processed = orderService.processOrder(order);
        return ResponseEntity.ok(processed);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId) {
        OrderDTO order = orderService.getByOrderId(orderId);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }

    @GetMapping("/by-cl-ord-id/{clOrdId}")
    public ResponseEntity<OrderDTO> getByClOrdId(@PathVariable String clOrdId) {
        OrderDTO order = orderService.getByClOrdId(clOrdId);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<Collection<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderStore.allOrders());
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> stats() {
        return ResponseEntity.ok(java.util.Map.of(
                "totalInMemory", orderStore.size(),
                "activeOrders",  orderStore.countActive()
        ));
    }
}
