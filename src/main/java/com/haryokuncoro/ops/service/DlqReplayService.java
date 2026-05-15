package com.haryokuncoro.ops.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.entity.FailedEvent;
import com.haryokuncoro.ops.repository.FailedEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DlqReplayService {
    private static final int MAX_RETRY = 3;

    private final FailedEventRepository repository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Transactional
    public void replay(UUID failedEventId) throws Exception {
        FailedEvent failedEvent = repository.findById(failedEventId).orElseThrow();
        if (failedEvent.getRetryCount() >= MAX_RETRY) {
            log.warn("Failed event {} has reached max retry count. Marking as permanently failed.", failedEventId);
            failedEvent.setStatus(FailedEvent.Status.PERMANENTLY_FAILED);
            repository.save(failedEvent);
            return;
        }

        try {
            failedEvent.setStatus(FailedEvent.Status.REPLAYING);
            failedEvent.setRetryCount(failedEvent.getRetryCount() + 1);
            repository.save(failedEvent);

            OrderCreatedEvent event = objectMapper.readValue(
                    failedEvent.getPayload(),
                    OrderCreatedEvent.class
            );

            kafkaTemplate.send(
                    failedEvent.getTopic(),
                    event.orderId().toString(),
                    event
            );

            failedEvent.setStatus(FailedEvent.Status.REPLAYED);
            repository.save(failedEvent);
            log.info("Replayed failed event {}", failedEventId);

        }catch (Exception e){
            log.error("Error replaying failed event {}: {}", failedEventId, e.getMessage());
            failedEvent.setStatus(FailedEvent.Status.FAILED);
            repository.save(failedEvent);
        }

    }

    public List<FailedEvent> findAll() {
        return repository.findAll();
    }
}