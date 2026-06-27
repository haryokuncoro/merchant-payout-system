package com.haryokuncoro.ops.dto;


import com.haryokuncoro.ops.dto.enums.FeeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class GetFeeConfigResponse {

    private UUID id;

    private UUID merchantId;

    private String merchantName;

    private FeeType feeType;

    private BigDecimal feeValue;

    private Boolean active;

    private Instant effectiveFrom;

    private Instant effectiveTo;

    private Instant createdAt;

    private Instant updatedAt;
}