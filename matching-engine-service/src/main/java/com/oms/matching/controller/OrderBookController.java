package com.oms.matching.controller;

import com.oms.matching.engine.MatchingEngine;
import com.oms.matching.engine.OrderBook;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class OrderBookController {

    private final MatchingEngine matchingEngine;

    @GetMapping("/books")
    public ResponseEntity<Map<String, Object>> getAllBooks() {
        Map<String, Object> result = new HashMap<>();
        matchingEngine.getOrderBooks().forEach((symbol, book) -> {
            Map<String, Object> bookInfo = new HashMap<>();
            bookInfo.put("bestBid", book.bestBid());
            bookInfo.put("bestAsk", book.bestAsk());
            bookInfo.put("bidLevels", book.bidLevels());
            bookInfo.put("askLevels", book.askLevels());
            result.put(symbol, bookInfo);
        });
        return ResponseEntity.ok(result);
    }

    @GetMapping("/books/{symbol}")
    public ResponseEntity<Map<String, Object>> getBook(@PathVariable String symbol) {
        OrderBook book = matchingEngine.getOrderBooks().get(symbol);
        if (book == null) return ResponseEntity.notFound().build();
        Map<String, Object> info = new HashMap<>();
        info.put("symbol", symbol);
        info.put("bestBid", book.bestBid());
        info.put("bestAsk", book.bestAsk());
        info.put("bidLevels", book.bidLevels());
        info.put("askLevels", book.askLevels());
        return ResponseEntity.ok(info);
    }

    @GetMapping("/latency")
    public ResponseEntity<Map<String, Object>> latencyStats() {
        return ResponseEntity.ok(Map.of(
            "avgLatencyMicros", matchingEngine.getLatencyTracker().avgMicros(),
            "avgLatencyMillis", matchingEngine.getLatencyTracker().avgMillis(),
            "totalOrders",      matchingEngine.getLatencyTracker().totalCount()
        ));
    }
}
