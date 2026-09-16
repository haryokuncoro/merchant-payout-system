package com.haryokuncoro.ops.event.producer;

import com.haryokuncoro.ops.dto.StripePayoutJobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripePayoutEventPublisher {

    private static final String TOPIC = "stripe.payout.created";

    @Value("${demo.mode}")
    private boolean demoMode;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(StripePayoutJobEvent event) {
        String key = event.getId().toString();

        if (demoMode) {
            log.info(
                    "Demo mode: Mock published StripePayoutJobEvent. topic={}, key={}, payload={}",
                    TOPIC, key, event
            );
            return;
        }

        log.info("Publishing StripePayoutJobEvent: {}", event);
        kafkaTemplate.send(TOPIC, key, event);
    }
}