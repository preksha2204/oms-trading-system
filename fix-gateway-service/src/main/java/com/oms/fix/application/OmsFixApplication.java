package com.oms.fix.application;

import com.oms.fix.handler.NewOrderSingleHandler;
import com.oms.fix.handler.OrderCancelHandler;
import com.oms.fix.store.SessionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import quickfix.*;
import quickfix.field.MsgType;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class OmsFixApplication implements Application {

    private final SessionStore           sessionStore;
    private final NewOrderSingleHandler  newOrderHandler;
    private final OrderCancelHandler     cancelHandler;

    @Override
    public void onCreate(SessionID sessionID) {
        log.info("[FIX] Session created: {}", sessionID);
    }

    @Override
    public void onLogon(SessionID sessionID) {
        log.info("[FIX] Logon: {}", sessionID);
        sessionStore.registerSession(sessionID);
    }

    @Override
    public void onLogout(SessionID sessionID) {
        log.info("[FIX] Logout: {}", sessionID);
        sessionStore.deregisterSession(sessionID);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
        log.debug("[FIX] toAdmin: {}", message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        log.debug("[FIX] fromAdmin: {}", message);
    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
        log.debug("[FIX] toApp: {}", message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        String msgType = message.getHeader().getString(MsgType.FIELD);
        log.debug("[FIX] fromApp msgType={} session={}", msgType, sessionID);

        switch (msgType) {
            case MsgType.ORDER_SINGLE ->
                newOrderHandler.handle((NewOrderSingle) message, sessionID);
            case MsgType.ORDER_CANCEL_REQUEST ->
                cancelHandler.handle((OrderCancelRequest) message, sessionID);
            default -> {
                log.warn("[FIX] Unsupported message type: {}", msgType);
                throw new UnsupportedMessageType();
            }
        }
    }
}
