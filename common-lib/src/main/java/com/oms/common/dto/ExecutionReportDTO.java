package com.oms.common.dto;

import com.oms.common.enums.ExecType;
import com.oms.common.enums.OrderSide;
import com.oms.common.enums.OrderStatus;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String      orderId;
    private String      clOrdId;
    private String      execId;
    private ExecType    execType;
    private OrderStatus ordStatus;
    private String      symbol;
    private OrderSide   side;
    private BigDecimal  lastQty;
    private BigDecimal  lastPx;
    private BigDecimal  cumQty;
    private BigDecimal  leavesQty;
    private BigDecimal  avgPx;
    private String      text;
    private String      fixSessionId;
    private Instant     transactTime;
}
