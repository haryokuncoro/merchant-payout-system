package com.haryokuncoro.ops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder @Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class OrderCreatedEvent extends  CreateOrderRequest{
    String eventId;
}