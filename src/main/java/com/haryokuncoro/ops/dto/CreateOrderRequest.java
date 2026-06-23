package com.haryokuncoro.ops.dto;

import com.haryokuncoro.ops.dto.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CreateOrderRequest{
    private String orderNo;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private String stripePaymentIntentId;
    private String stripeChargeId;
    private PaymentStatus paymentStatus;
    private String paidAt;
}