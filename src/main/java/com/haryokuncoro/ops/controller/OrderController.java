package com.haryokuncoro.ops.controller;

import com.haryokuncoro.ops.dto.CreateOrderRequest;
import com.haryokuncoro.ops.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> create(
            @RequestBody CreateOrderRequest request
    ) {

        UUID orderId =
                orderService.createOrder(request);

        return ResponseEntity.ok(orderId.toString());
    }
}