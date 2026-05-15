package com.haryokuncoro.ops.event.consumer;


import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class InventoryConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "order.created",
            groupId = "inventory-group"
    )
    public void consume(OrderCreatedEvent event) {
        log.info("Received order event {}", event.orderId());
        inventoryService.reserve(event);
    }
}