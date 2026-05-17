package com.haryokuncoro.ops.event.consumer;


import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class InventoryConsumer {

    private final InventoryService inventoryService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(
                    delay = 2000,
                    multiplier = 2.0
            ),
            dltTopicSuffix = ".dlq"
    )
    @KafkaListener(
            topics = "order.created",
            groupId = "inventory-group",
            containerFactory = "manualAckFactory",
            concurrency = "1"
    )
    public void consume(OrderCreatedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received order event {}", event.orderId());
            inventoryService.reserve(event);
            acknowledgment.acknowledge();
            log.info("Inventory reserved successfully for orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("Error reserving inventory for orderId={}, error={}", event.orderId(), e.getMessage());
            throw e;
        }

    }
}