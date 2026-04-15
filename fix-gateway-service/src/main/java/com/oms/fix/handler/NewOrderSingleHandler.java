package com.oms.fix.handler;

import com.oms.common.dto.OrderDTO;
import com.oms.common.enums.OrderSide;
import com.oms.common.enums.OrderStatus;
import com.oms.common.enums.OrderType;
import com.oms.fix.kafka.FixOrderProducer;
import com.oms.fix.validation.FixMessageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix44.NewOrderSingle;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewOrderSingleHandler {

    private final FixMessageValidator validator;
    private final FixOrderProducer    producer;

    public void handle(NewOrderSingle nos, SessionID sessionID) {
        long nanoStart = System.nanoTime();
        try {
            OrderDTO order = parse(nos, sessionID);
            validator.validate(order);
            producer.publishOrder(order);
            log.info("NewOrderSingle processed: ClOrdID={} Symbol={} Side={} Qty={} Price={}",
                    order.getClOrdId(), order.getSymbol(),
                    order.getSide(), order.getQuantity(), order.getPrice());
        } catch (FieldNotFound e) {
            log.error("Missing FIX field: tag={}", e.field, e);
        } catch (IllegalArgumentException e) {
            log.warn("Order validation failed: {}", e.getMessage());
        } finally {
            log.debug("NewOrderSingle handler took {} µs",
                    (System.nanoTime() - nanoStart) / 1_000);
        }
    }

    private OrderDTO parse(NewOrderSingle nos, SessionID sessionID) throws FieldNotFound {
        String clOrdId = nos.getString(ClOrdID.FIELD);
        String symbol  = nos.getString(Symbol.FIELD);
        char   side    = nos.getChar(Side.FIELD);
        double qty     = nos.getDouble(OrderQty.FIELD);
        double price   = nos.isSetField(Price.FIELD) ? nos.getDouble(Price.FIELD) : 0.0;
        char   ordType = nos.getChar(OrdType.FIELD);

        return OrderDTO.builder()
                .clOrdId(clOrdId)
                .symbol(symbol)
                .side(OrderSide.fromFix(side))
                .orderType(OrderType.fromFix(ordType))
                .quantity(BigDecimal.valueOf(qty))
                .filledQty(BigDecimal.ZERO)
                .price(BigDecimal.valueOf(price))
                .status(OrderStatus.NEW)
                .fixSessionId(sessionID.toString())
                .receivedAt(Instant.now())
                .receiveNanos(System.nanoTime())
                .build();
    }
}
