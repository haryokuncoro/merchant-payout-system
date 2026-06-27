package com.haryokuncoro.ops.dto;


import com.haryokuncoro.ops.dto.enums.FeeType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class UpdateFeeConfigRequest {

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