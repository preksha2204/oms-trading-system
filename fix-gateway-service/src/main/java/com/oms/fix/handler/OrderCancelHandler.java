package com.oms.fix.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.OrigClOrdID;
import quickfix.fix44.OrderCancelRequest;

@Slf4j
@Component
public class OrderCancelHandler {

    public void handle(OrderCancelRequest cancelReq, SessionID sessionID) {
        try {
            String clOrdId     = cancelReq.getString(ClOrdID.FIELD);
            String origClOrdId = cancelReq.getString(OrigClOrdID.FIELD);
            log.info("OrderCancelRequest: ClOrdID={} OrigClOrdID={} Session={}",
                    clOrdId, origClOrdId, sessionID);
            // TODO: publish order.cancel event to Kafka
        } catch (FieldNotFound e) {
            log.error("Missing field in OrderCancelRequest: tag={}", e.field, e);
        }
    }
}
