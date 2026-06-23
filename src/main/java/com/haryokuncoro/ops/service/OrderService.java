package com.haryokuncoro.ops.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.haryokuncoro.ops.dto.CreateOrderRequest;
import com.haryokuncoro.ops.dto.OrderCreatedEvent;
import com.haryokuncoro.ops.entity.BillingOrder;
import com.haryokuncoro.ops.event.producer.OrderEventPublisher;
import com.haryokuncoro.ops.repository.BillingOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional @Slf4j
public class OrderService {
    private static final BigDecimal FEE_RATE = new BigDecimal("0.01");
    private static final ObjectMapper mapper = new ObjectMapper();
    private final BillingOrderRepository repository;
    private final OrderEventPublisher publisher;

    public String publishOrder(CreateOrderRequest request) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OrderCreatedEvent event = mapper.convertValue(request, OrderCreatedEvent.class);
        publisher.publish(event);
        return request.getOrderNo();
    }

    @Transactional
    public void createOrder(OrderCreatedEvent event){
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        BillingOrder order = mapper.convertValue(event, BillingOrder.class);
        repository.save(order);
        log.info("saved order data");
    }

    private BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}