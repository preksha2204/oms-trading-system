package com.oms.order.repository;

import com.oms.common.enums.OrderStatus;
import com.oms.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByClOrdId(String clOrdId);

    List<OrderEntity> findBySymbolAndStatus(String symbol, OrderStatus status);

    Page<OrderEntity> findBySymbol(String symbol, Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.receivedAt BETWEEN :from AND :to")
    List<OrderEntity> findByTimeRange(@Param("from") Instant from, @Param("to") Instant to);

    long countByStatus(OrderStatus status);
}
