package com.haryokuncoro.ops.service;


import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InventoryService {

    public void reserve(OrderCreatedEvent event) {

        log.info(
                "Reserving inventory for order {}",
                event.orderId()
        );
    }
}