package com.haryokuncoro.ops.dto;

import com.haryokuncoro.ops.dto.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class CreateOrderRequest{
    private String orderNo;
    private UUID merchantId;
    private BigDecimal amount;
    private String currency;
    private String stripePaymentIntentId;
    private PaymentStatus paymentStatus;
    private String paidAt;
}