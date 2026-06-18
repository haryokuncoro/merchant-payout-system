package com.haryokuncoro.ops.event.producer;

import com.haryokuncoro.ops.dto.PayoutJobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayoutEventPublisher {
    private static final String TOPIC = "payout.created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(PayoutJobEvent event) {
        log.info("Publishing PayoutJobEvent: {}", event);
        String key = event.getEventId().toString();
        kafkaTemplate.send(
                TOPIC,
                key,
                event
        );
    }

}
