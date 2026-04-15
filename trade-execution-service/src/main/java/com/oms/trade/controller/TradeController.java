package com.oms.trade.controller;

import com.oms.common.dto.TradeDTO;
import com.oms.trade.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<Page<TradeDTO>> getBySymbol(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(tradeService.getTradesBySymbol(symbol, page, size));
    }

    @GetMapping("/symbol/{symbol}/range")
    public ResponseEntity<List<TradeDTO>> getBySymbolAndTimeRange(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseEntity.ok(tradeService.getTradesBySymbolAndTime(symbol, from, to));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of("persistQueueSize", tradeService.queueSize()));
    }
}
