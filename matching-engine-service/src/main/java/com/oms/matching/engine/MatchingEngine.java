package com.oms.matching.engine;

import com.oms.common.dto.OrderDTO;
import com.oms.common.dto.TradeDTO;
import com.oms.common.enums.OrderSide;
import com.oms.common.enums.OrderStatus;
import com.oms.common.util.LatencyTracker;
import com.oms.matching.kafka.TradeProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Price-Time Priority Matching Engine.
 *
 * Rules:
 *  - Buy  order matches against lowest asks  (best ask first)
 *  - Sell order matches against highest bids (best bid first)
 *  - Trade price = RESTING order price (not aggressor)
 *  - Partial fills supported across multiple price levels
 *  - Residual quantity added to book if not fully filled
 */
@Slf4j
@Service
public class MatchingEngine {

    private final ConcurrentHashMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final TradeProducer    tradeProducer;
    private final LatencyTracker   latencyTracker;

    public MatchingEngine(TradeProducer tradeProducer) {
        this.tradeProducer  = tradeProducer;
        this.latencyTracker = new LatencyTracker();
    }

    public List<TradeDTO> match(OrderDTO aggressor) {
        long startNano = latencyTracker.startNano();
        List<TradeDTO> trades = new ArrayList<>();

        OrderBook book = orderBooks.computeIfAbsent(aggressor.getSymbol(), OrderBook::new);

        if (aggressor.getSide() == OrderSide.BUY) {
            matchAgainstAsks(aggressor, book, trades);
        } else {
            matchAgainstBids(aggressor, book, trades);
        }

        // Add residual to book if not fully filled
        if (remaining(aggressor).compareTo(BigDecimal.ZERO) > 0) {
            book.addOrder(aggressor);
            log.debug("Residual order added to book: orderId={} remaining={}",
                    aggressor.getOrderId(), remaining(aggressor));
        }

        latencyTracker.record(startNano);
        latencyTracker.logStats(1000);

        trades.forEach(trade -> tradeProducer.publish(trade));
        return trades;
    }

    // ---------- Buy side: match against asks (lowest ask first) ----------
    private void matchAgainstAsks(OrderDTO buy, OrderBook book, List<TradeDTO> trades) {
        while (remaining(buy).compareTo(BigDecimal.ZERO) > 0) {
            Map.Entry<BigDecimal, PriceLevel> bestAskEntry = book.getAsks().firstEntry();
            if (bestAskEntry == null) break;

            BigDecimal bestAskPrice = bestAskEntry.getKey();

            // Match condition: buy price >= best ask price
            if (buy.getPrice().compareTo(bestAskPrice) < 0) break;

            PriceLevel level  = bestAskEntry.getValue();
            OrderDTO   resting = level.peek();
            if (resting == null) {
                book.getAsks().remove(bestAskPrice);
                continue;
            }

            BigDecimal tradePrice = resting.getPrice();              // ← resting order price
            BigDecimal tradeQty   = remaining(buy).min(remaining(resting));

            fill(buy,     tradeQty);
            fill(resting, tradeQty);

            trades.add(buildTrade(buy, resting, tradeQty, tradePrice));
            log.info("[MATCH] BUY {} @ {} matched SELL {} @ {} qty={}",
                    buy.getOrderId(), buy.getPrice(),
                    resting.getOrderId(), tradePrice, tradeQty);

            if (remaining(resting).compareTo(BigDecimal.ZERO) <= 0) {
                level.poll();
                if (level.isEmpty()) book.getAsks().remove(bestAskPrice);
            }
        }
    }

    // ---------- Sell side: match against bids (highest bid first) ----------
    private void matchAgainstBids(OrderDTO sell, OrderBook book, List<TradeDTO> trades) {
        while (remaining(sell).compareTo(BigDecimal.ZERO) > 0) {
            Map.Entry<BigDecimal, PriceLevel> bestBidEntry = book.getBids().firstEntry();
            if (bestBidEntry == null) break;

            BigDecimal bestBidPrice = bestBidEntry.getKey();

            // Match condition: sell price <= best bid price
            if (sell.getPrice().compareTo(bestBidPrice) > 0) break;

            PriceLevel level   = bestBidEntry.getValue();
            OrderDTO   resting = level.peek();
            if (resting == null) {
                book.getBids().remove(bestBidPrice);
                continue;
            }

            BigDecimal tradePrice = resting.getPrice();              // ← resting order price
            BigDecimal tradeQty   = remaining(sell).min(remaining(resting));

            fill(sell,    tradeQty);
            fill(resting, tradeQty);

            trades.add(buildTrade(resting, sell, tradeQty, tradePrice));
            log.info("[MATCH] SELL {} @ {} matched BUY {} @ {} qty={}",
                    sell.getOrderId(), sell.getPrice(),
                    resting.getOrderId(), tradePrice, tradeQty);

            if (remaining(resting).compareTo(BigDecimal.ZERO) <= 0) {
                level.poll();
                if (level.isEmpty()) book.getBids().remove(bestBidPrice);
            }
        }
    }

    private TradeDTO buildTrade(OrderDTO buyOrder, OrderDTO sellOrder,
                                 BigDecimal qty, BigDecimal price) {
        String tradeId        = UUID.randomUUID().toString();
        String idempotencyKey = buyOrder.getOrderId() + ":" + sellOrder.getOrderId()
                              + ":" + price.toPlainString() + ":" + qty.toPlainString();
        return TradeDTO.builder()
                .tradeId(tradeId)
                .buyOrderId(buyOrder.getOrderId())
                .sellOrderId(sellOrder.getOrderId())
                .buyClOrdId(buyOrder.getClOrdId())
                .sellClOrdId(sellOrder.getClOrdId())
                .symbol(buyOrder.getSymbol())
                .quantity(qty)
                .price(price)
                .execId("X-" + System.nanoTime())
                .idempotencyKey(idempotencyKey)
                .executedAt(Instant.now())
                .build();
    }

    private BigDecimal remaining(OrderDTO o) {
        BigDecimal rem = o.getQuantity().subtract(
                o.getFilledQty() != null ? o.getFilledQty() : BigDecimal.ZERO);
        return rem.max(BigDecimal.ZERO);
    }

    private void fill(OrderDTO o, BigDecimal qty) {
        BigDecimal filled = (o.getFilledQty() != null ? o.getFilledQty() : BigDecimal.ZERO).add(qty);
        o.setFilledQty(filled);
        o.setStatus(filled.compareTo(o.getQuantity()) >= 0 ? OrderStatus.FILLED : OrderStatus.PARTIAL);
    }

    public Map<String, OrderBook> getOrderBooks() { return orderBooks; }

    public LatencyTracker getLatencyTracker() { return latencyTracker; }
}
