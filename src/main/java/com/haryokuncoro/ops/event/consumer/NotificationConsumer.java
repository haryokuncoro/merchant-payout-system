package com.haryokuncoro.ops.event.consumer;


import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "order.created",
            groupId = "notification-group"
    )
    public void consume(OrderCreatedEvent event) {
        log.info("Received order event {}", event.orderId());
        notificationService.send(event);
    }
}