package com.haryokuncoro.ops.dto;

import com.haryokuncoro.ops.dto.enums.OrderType;
import lombok.Data;

@Data
public class CreatePayoutJobRequest {
    OrderType type;
    String billingCycle;
}
