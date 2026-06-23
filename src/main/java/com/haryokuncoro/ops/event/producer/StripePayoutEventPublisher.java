package com.haryokuncoro.ops.event.producer;

import com.haryokuncoro.ops.dto.StripePayoutJobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripePayoutEventPublisher {
    private static final String TOPIC = "stripe.payout.created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(StripePayoutJobEvent event) {
        log.info("Publishing StripePayoutJobEvent: {}", event);
        String key = event.getId().toString();
        kafkaTemplate.send(
                TOPIC,
                key,
                event
        );
    }
}
