package com.haryokuncoro.ops.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.entity.FailedEvent;
import com.haryokuncoro.ops.repository.FailedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDlqConsumer {

    private final FailedEventRepository repository;

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "order.created.dlq",
            groupId = "payment-dlq-group"
    )
    public void consume(OrderCreatedEvent event) throws Exception {
        log.error("DLQ EVENT RECEIVED {}", event.getOrderNo());

        FailedEvent failedEvent = new FailedEvent();

        failedEvent.setId(UUID.randomUUID());

        failedEvent.setTopic(
                "order.created"
        );

        failedEvent.setEventId(
                UUID.randomUUID()
        );

        failedEvent.setPayload(
                objectMapper.writeValueAsString(event)
        );

        failedEvent.setErrorMessage(
                "Payment processing failed"
        );
        failedEvent.setRetryCount(0);
        failedEvent.setStatus(FailedEvent.Status.FAILED);
        failedEvent.setCreatedAt(Instant.now());
        repository.save(failedEvent);
    }
}