package com.haryokuncoro.ops.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        Instant createdAt
){};