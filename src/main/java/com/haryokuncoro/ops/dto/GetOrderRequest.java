package com.haryokuncoro.ops.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter @Builder
public class GetOrderRequest {
    UUID merchantId;
}
