package com.haryokuncoro.ops.dto;

import com.haryokuncoro.ops.dto.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreatePayoutRequest {
    UUID merchantId;
    OrderType type;
    String billingCycle;
}
