package com.oms.refdata.controller;

import com.oms.refdata.entity.CustomerMaster;
import com.oms.refdata.entity.SecurityMaster;
import com.oms.refdata.service.ReferenceDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ref-data")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final ReferenceDataService service;

    @GetMapping("/symbols/validate")
    public ResponseEntity<Boolean> validateSymbol(@RequestParam String symbol) {
        return ResponseEntity.ok(service.isValidSymbol(symbol));
    }

    @GetMapping("/symbols/{symbol}")
    public ResponseEntity<SecurityMaster> getSecurity(@PathVariable String symbol) {
        return service.getSecurity(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/symbols")
    public ResponseEntity<List<SecurityMaster>> getAllSecurities() {
        return ResponseEntity.ok(service.getAllActiveSecurities());
    }

    @GetMapping("/symbols/{symbol}/options")
    public ResponseEntity<List<SecurityMaster>> getOptions(@PathVariable String symbol) {
        return ResponseEntity.ok(service.getOptionsByUnderlying(symbol));
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerMaster> getCustomer(@PathVariable String customerId) {
        return service.getCustomer(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/cache/reload")
    public ResponseEntity<Map<String, String>> reloadCache() {
        service.reloadCache();
        return ResponseEntity.ok(Map.of("status", "reloaded"));
    }
}
