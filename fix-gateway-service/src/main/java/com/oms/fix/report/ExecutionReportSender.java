package com.oms.fix.report;

import com.oms.common.dto.ExecutionReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.ExecutionReport;

import java.util.Date;

@Slf4j
@Component
public class ExecutionReportSender {

    public void send(ExecutionReportDTO dto) {
        try {
            ExecutionReport report = new ExecutionReport(
                new OrderID(dto.getOrderId()),
                new ExecID(dto.getExecId()),
                new ExecType(dto.getExecType().getFixValue()),
                new OrdStatus(mapStatus(dto.getOrdStatus().name())),
                new Symbol(dto.getSymbol()),
                new Side(dto.getSide().getFixValue()),
                new LeavesQty(dto.getLeavesQty() != null ? dto.getLeavesQty().doubleValue() : 0),
                new CumQty(dto.getCumQty() != null ? dto.getCumQty().doubleValue() : 0),
                new AvgPx(dto.getAvgPx() != null ? dto.getAvgPx().doubleValue() : 0)
            );
            report.set(new ClOrdID(dto.getClOrdId()));
            if (dto.getLastQty() != null) report.set(new LastQty(dto.getLastQty().doubleValue()));
            if (dto.getLastPx() != null) report.set(new LastPx(dto.getLastPx().doubleValue()));
            report.set(new TransactTime(new Date()));
            if (dto.getText() != null) report.set(new Text(dto.getText()));

            SessionID sessionId = parseSessionId(dto.getFixSessionId());
            boolean sent = Session.sendToTarget(report, sessionId);
            if (sent) {
                log.info("ExecutionReport sent: ClOrdID={} ExecType={}", dto.getClOrdId(), dto.getExecType());
            } else {
                log.warn("Failed to send ExecutionReport for ClOrdID={}", dto.getClOrdId());
            }
        } catch (SessionNotFound e) {
            log.error("FIX Session not found: {}", dto.getFixSessionId(), e);
        }
    }

    private char mapStatus(String status) {
        return switch (status) {
            case "NEW"       -> OrdStatus.NEW;
            case "PARTIAL"   -> OrdStatus.PARTIALLY_FILLED;
            case "FILLED"    -> OrdStatus.FILLED;
            case "CANCELLED" -> OrdStatus.CANCELED;
            case "REJECTED"  -> OrdStatus.REJECTED;
            default          -> OrdStatus.NEW;
        };
    }

    private SessionID parseSessionId(String raw) {
        // Format: "FIX.4.4:SENDER->TARGET"
        if (raw == null) return null;
        String[] parts = raw.split(":");
        String[] comp = parts.length > 1 ? parts[1].split("->") : new String[]{"OMS_SERVER","TRADING_CLIENT"};
        return new SessionID("FIX.4.4", comp[0], comp[1]);
    }
}
