package com.oms.trade.repository;

import com.oms.trade.entity.TradeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, String> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<TradeEntity> findBySymbol(String symbol, Pageable pageable);

    @Query("SELECT t FROM TradeEntity t WHERE t.symbol = :symbol AND t.executedAt BETWEEN :from AND :to ORDER BY t.executedAt DESC")
    List<TradeEntity> findBySymbolAndTimeRange(@Param("symbol") String symbol,
                                               @Param("from") Instant from,
                                               @Param("to") Instant to);

    @Query("SELECT t FROM TradeEntity t WHERE t.executedAt BETWEEN :from AND :to ORDER BY t.executedAt DESC")
    List<TradeEntity> findByTimeRange(@Param("from") Instant from, @Param("to") Instant to);

    long countBySymbol(String symbol);
}
