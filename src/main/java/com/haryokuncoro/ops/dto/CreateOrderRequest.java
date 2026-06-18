package com.haryokuncoro.ops.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(
        UUID orderId,
        UUID customerId,
        UUID merchantId,
        OrderType type,
        BigDecimal amount
) {
}