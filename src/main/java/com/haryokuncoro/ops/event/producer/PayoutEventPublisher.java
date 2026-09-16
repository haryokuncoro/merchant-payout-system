package com.haryokuncoro.ops.event.producer;

import com.haryokuncoro.ops.dto.PayoutJobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayoutEventPublisher {

    private static final String TOPIC = "payout.created";

    @Value("${demo.mode}")
    private boolean demoMode;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(PayoutJobEvent event) {
        String key = event.getEventId().toString();

        if (demoMode) {
            log.info(
                    "Demo mode: Mock published PayoutJobEvent. topic={}, key={}, payload={}",
                    TOPIC, key, event
            );
            return;
        }

        log.info("Publishing PayoutJobEvent: {}", event);
        kafkaTemplate.send(TOPIC, key, event);
    }
}