package com.haryokuncoro.ops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @Builder
public class StripePayoutJobEvent {
    UUID id;
    UUID merchantId;
}
