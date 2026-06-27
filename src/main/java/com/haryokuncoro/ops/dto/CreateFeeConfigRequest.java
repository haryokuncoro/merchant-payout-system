package com.haryokuncoro.ops.dto;


import com.haryokuncoro.ops.dto.enums.FeeType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class CreateFeeConfigRequest {

    @NotNull
    private UUID merchantId;

    @NotNull
    private FeeType feeType;

    @NotNull
    private BigDecimal feeValue;

    @NotNull
    private Boolean active;

    @NotNull
    private Instant effectiveFrom;

    private Instant effectiveTo;
}