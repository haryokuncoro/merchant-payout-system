package com.haryokuncoro.ops.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreatePayoutRequest {
    UUID merchantId;
    PayoutType type;
    String billingCycle;
}
