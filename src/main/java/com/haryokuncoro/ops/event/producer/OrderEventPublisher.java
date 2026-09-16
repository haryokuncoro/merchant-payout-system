package com.haryokuncoro.ops.event.producer;

import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventPublisher {

    private static final String TOPIC = "order.created";

    @Value("${demo.mode}")
    private boolean demoMode;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(OrderCreatedEvent event) {
        String key = event.getOrderNo().toString();

        if (demoMode) {
            log.info("Demo mode: Mock publishing OrderCreatedEvent. topic={}, key={}, payload={}",
                    TOPIC, key, event);
            return;
        }

        log.info("Publishing OrderCreatedEvent: {}", event);
        kafkaTemplate.send(TOPIC, key, event);
    }
}