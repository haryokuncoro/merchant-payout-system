package com.haryokuncoro.ops.event.consumer;

import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderService orderService;

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
            groupId = "payment-group",
            containerFactory = "manualAckFactory",
            concurrency = "1"
    )
    public void consume(OrderCreatedEvent event, Acknowledgment acknowledgment) {
        String orderId = event.eventId().toString();
        try {
            log.info("Received order event {}", orderId);
            orderService.createOrder(event);
            acknowledgment.acknowledge();
            log.info("Order processed successfully for orderId={}", orderId);
        }catch (Exception e) {
            log.error("Error processing order ={}, error={}", orderId, e.getMessage());
            throw e;
        }

    }
}