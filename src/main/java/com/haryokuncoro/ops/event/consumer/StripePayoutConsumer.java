package com.haryokuncoro.ops.event.consumer;

import com.haryokuncoro.ops.dto.PayoutJobEvent;
import com.haryokuncoro.ops.dto.StripePayoutJobEvent;
import com.haryokuncoro.ops.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePayoutConsumer {
    private final PayoutService payoutService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(
                    delay = 2000,
                    multiplier = 2.0
            ),
            dltTopicSuffix = ".dlq"
    )
    @KafkaListener(
            topics = "stripe.payout.created",
            groupId = "payout-group",
            containerFactory = "manualAckFactory",
            concurrency = "1"
    )
    public void consume(StripePayoutJobEvent event, Acknowledgment acknowledgment) {
        String eventId = event.getId().toString();
        try {
            payoutService.handleStripePayoutJob(event);
            acknowledgment.acknowledge();
        }catch (Exception e) {
            log.error("Error processing payout ={}, error={}", eventId, e.getMessage());
            throw e;
        }

    }
}
