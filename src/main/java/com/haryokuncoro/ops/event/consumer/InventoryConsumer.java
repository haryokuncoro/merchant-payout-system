package com.haryokuncoro.ops.event.consumer;


import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "order.created",
            groupId = "inventory-group"
    )
    public void consume(OrderCreatedEvent event) {

        inventoryService.reserve(event);
    }
}