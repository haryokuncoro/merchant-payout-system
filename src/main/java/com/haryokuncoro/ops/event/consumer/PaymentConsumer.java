package com.haryokuncoro.ops.event.consumer;

import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final PaymentService paymentService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000),
            dltTopicSuffix = ".dlq"
    )
    @KafkaListener(
            topics = "order.created",
            groupId = "payment-group"
    )
    public void consume(OrderCreatedEvent event) {

        log.info(
                "Received order event {}",
                event.orderId()
        );

        paymentService.processPayment(event);
    }
}