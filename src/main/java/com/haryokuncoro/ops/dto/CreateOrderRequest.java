package com.haryokuncoro.ops.dto;

import com.haryokuncoro.ops.dto.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class CreateOrderRequest{
    private String orderNo;
    private UUID merchantId;
    private BigDecimal amount;
    @Schema(defaultValue = "USD")
    private String currency;
    @Schema(defaultValue = "pi_test0001")
    private String stripePaymentIntentId;
    @Schema(defaultValue = "PAID")
    private PaymentStatus paymentStatus;
    @Schema(example = "2026-06-23T10:00:00Z", type = "string", format = "date-time")
    private String paidAt;
}