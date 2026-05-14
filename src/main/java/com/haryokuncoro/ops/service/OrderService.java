package com.haryokuncoro.ops.service;

import com.haryokuncoro.ops.dto.CreateOrderRequest;
import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.entity.Order;
import com.haryokuncoro.ops.event.producer.OrderEventPublisher;
import com.haryokuncoro.ops.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository repository;
    private final OrderEventPublisher publisher;

    public UUID createOrder(CreateOrderRequest request) {

        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(request.customerId());
        order.setAmount(request.amount());
        order.setStatus("CREATED");
        order.setCreatedAt(Instant.now());

        repository.save(order);

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        UUID.randomUUID(),
                        orderId,
                        request.customerId(),
                        request.amount(),
                        Instant.now()
                );

        publisher.publish(event);

        return orderId;
    }
}