package com.haryokuncoro.ops.event.producer;


import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class OrderEventPublisher {
    private static final String TOPIC = "order.created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent: {}", event);
        String key = event.orderId().toString();
        kafkaTemplate.send(
                TOPIC,
                key,
                event
        );
    }
}