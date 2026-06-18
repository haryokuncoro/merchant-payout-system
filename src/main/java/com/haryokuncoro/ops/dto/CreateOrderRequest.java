package com.haryokuncoro.ops.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(
        UUID customerId,
        UUID merchantId,
        String type, //ORDER, TERMINAL, RESERVE
        BigDecimal amount
) {
}