package com.haryokuncoro.ops.service;

import com.haryokuncoro.ops.dto.CreateOrderRequest;
import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.entity.Order;
import com.haryokuncoro.ops.event.producer.OrderEventPublisher;
import com.haryokuncoro.ops.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional @Slf4j
public class OrderService {
    private static final BigDecimal FEE_RATE = new BigDecimal("0.01");

    private final OrderRepository repository;
    private final OrderEventPublisher publisher;

    public UUID publishOrder(CreateOrderRequest request) {
        UUID orderId = request.orderId();
        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        orderId,
                        request.customerId(),
                        request.merchantId(),
                        request.type(),
                        request.amount(),
                        Instant.now()
                );
        publisher.publish(event);
        return orderId;
    }

    @Transactional
    public void createOrder(OrderCreatedEvent event){
        if (repository.existsById(event.eventId())) {
            log.warn("Order already exists. orderId={}", event.eventId());
            return;
        }
        BigDecimal feeAmount = calculateFee(event.amount());
        BigDecimal disburseAmount = event.amount().subtract(feeAmount);

        Order order = Order.builder()
                .id(event.eventId())
                .merchantId(event.merchantId())
                .customerId(event.customerId())
                .type(event.type())
                .amount(event.amount())
                .feeAmount(feeAmount)
                .disburseAmount(disburseAmount)
                .status("CREATED")
                .createdAt(Instant.now())
                .build();
        repository.save(order);
        log.info("saved order data");
    }

    private BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}