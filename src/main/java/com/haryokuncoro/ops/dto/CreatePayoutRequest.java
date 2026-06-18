package com.haryokuncoro.ops.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreatePayoutRequest {
    UUID merchantId;
    OrderType type;
    String billingCycle;
}
