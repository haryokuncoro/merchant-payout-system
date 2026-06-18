package com.haryokuncoro.ops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor @NoArgsConstructor
public class PayoutJobEvent {
    UUID eventId;
    UUID merchantId;
    OrderType type;
    String billingCycle;
}
