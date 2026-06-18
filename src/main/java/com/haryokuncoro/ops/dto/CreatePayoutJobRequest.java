package com.haryokuncoro.ops.dto;

import lombok.Data;

@Data
public class CreatePayoutJobRequest {
    OrderType type;
    String billingCycle;
}
