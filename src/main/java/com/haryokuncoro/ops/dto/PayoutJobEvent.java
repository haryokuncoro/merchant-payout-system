package com.haryokuncoro.ops.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor @NoArgsConstructor
public class PayoutJobEvent {
    UUID eventId;
    UUID merchantId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodStart;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodEnd;
}
