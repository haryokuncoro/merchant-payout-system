package com.haryokuncoro.ops.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @Builder
public class FeeSummary {
    private BigDecimal grossAmount;
    private BigDecimal totalFee;
    private BigDecimal netAmount;
}

