package com.haryokuncoro.ops.dto;

import com.haryokuncoro.ops.dto.enums.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID customerId,
        UUID merchantId,
        OrderType type,
        BigDecimal amount,
        Instant createdAt
){};