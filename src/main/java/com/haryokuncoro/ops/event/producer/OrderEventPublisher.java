package com.haryokuncoro.ops.event.producer;


import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(OrderCreatedEvent event) {

        kafkaTemplate.send(
                "order.created",
                event.orderId().toString(),
                event
        );
    }
}