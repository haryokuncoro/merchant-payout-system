package com.haryokuncoro.ops.event.consumer;


import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "order.created",
            groupId = "notification-group"
    )
    public void consume(OrderCreatedEvent event) {

        notificationService.send(event);
    }
}