package com.haryokuncoro.ops.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class OrderCreatedEvent extends  CreateOrderRequest{
    String eventId;
}