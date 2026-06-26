package com.haryokuncoro.ops.dto;

import com.haryokuncoro.ops.dto.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Getter @Setter @Builder
public class GetOrderResponse {
    UUID id;
    UUID merchantId;
    String orderNo;
    BigDecimal amount;
    String currency;
    PaymentStatus paymentStatus;
    Instant paidAt;
}
