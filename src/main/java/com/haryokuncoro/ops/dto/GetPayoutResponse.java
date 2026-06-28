package com.haryokuncoro.ops.dto;

import com.haryokuncoro.ops.dto.enums.PayoutStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder
public class GetPayoutResponse {
    private UUID id;
    private UUID merchantId;
    private String merchantName;
    private String periodStart;
    private String periodEnd;
    private String currency;
    private BigDecimal grossAmount;
    private BigDecimal feeAmount;
    private BigDecimal payoutAmount;
    private String stripePayoutId;
    private Instant payoutDate;
    private PayoutStatus status;
}
