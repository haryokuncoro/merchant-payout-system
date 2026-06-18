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

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional @Slf4j
public class OrderService {

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
        Order order = Order.builder()
                .id(event.eventId())
                .merchantId(event.merchantId())
                .customerId(event.customerId())
                .type(event.type())
                .amount(event.amount())
                .status("CREATED")
                .createdAt(Instant.now())
                .build();
        repository.save(order);
        log.info("saved order data");
    }
}