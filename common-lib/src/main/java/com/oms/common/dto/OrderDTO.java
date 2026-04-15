package com.oms.common.dto;

import com.oms.common.enums.ExecType;
import com.oms.common.enums.OrderSide;
import com.oms.common.enums.OrderStatus;
import com.oms.common.enums.OrderType;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long        orderId;
    private String      clOrdId;
    private String      symbol;
    private String      customerId;
    private OrderSide   side;
    private OrderType   orderType;
    private BigDecimal  quantity;
    private BigDecimal  filledQty;
    private BigDecimal  price;
    private OrderStatus status;
    private String      fixSessionId;
    private Instant     receivedAt;
    private long        receiveNanos;
    private String      exchange;
    private String      currency;
}
