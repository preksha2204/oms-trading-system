package com.oms.refdata.cache;

import com.oms.refdata.entity.SecurityMaster;
import com.oms.refdata.repository.SecurityMasterRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory security master cache — preloaded at startup.
 * Eliminates DB round-trip for symbol validation on critical order path.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityMasterCache {

    private final SecurityMasterRepository repository;
    private final ConcurrentHashMap<String, SecurityMaster> cache = new ConcurrentHashMap<>(10_000);

    @PostConstruct
    public void load() {
        List<SecurityMaster> securities = repository.findByIsActiveTrue();
        securities.forEach(s -> cache.put(s.getSymbol(), s));
        log.info("SecurityMasterCache loaded {} securities", cache.size());
    }

    public boolean isValidSymbol(String symbol) {
        return cache.containsKey(symbol);
    }

    public SecurityMaster get(String symbol) {
        return cache.get(symbol);
    }

    public List<SecurityMaster> getOptionsByUnderlying(String underlyingSymbol) {
        return cache.values().stream()
                .filter(s -> underlyingSymbol.equals(s.getUnderlyingSymbol())
                          && s.getOptionType() != null)
                .toList();
    }

    public int size() { return cache.size(); }

    public void reload() {
        cache.clear();
        load();
        log.info("SecurityMasterCache reloaded");
    }
}
