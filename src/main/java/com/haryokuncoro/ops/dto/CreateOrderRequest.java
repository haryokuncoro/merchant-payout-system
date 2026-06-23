package com.haryokuncoro.ops.dto;

import com.haryokuncoro.ops.dto.enums.OrderType;

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