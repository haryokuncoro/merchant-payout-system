package com.haryokuncoro.ops.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentCompletedEvent(
        UUID eventId,
        UUID orderId,
        boolean success,
        Instant createdAt
) {
}