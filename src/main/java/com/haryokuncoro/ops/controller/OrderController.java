package com.haryokuncoro.ops.controller;

import com.haryokuncoro.ops.dto.ApiResponse;
import com.haryokuncoro.ops.dto.CreateOrderRequest;
import com.haryokuncoro.ops.dto.GetOrderResponse;
import com.haryokuncoro.ops.service.OrderService;
import com.haryokuncoro.ops.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> create(@RequestBody CreateOrderRequest request) {
        String orderNumber = orderService.publishOrder(request);
        return ResponseEntity.ok(
                ResponseUtil.success("", orderNumber)
        );
    }

    @GetMapping
    public Page<GetOrderResponse> getBillingOrders(
            @RequestParam(required = false) UUID merchantId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String stripePaymentIntentId,
            Pageable pageable) {

        return orderService.search(
                merchantId,
                orderNo,
                stripePaymentIntentId,
                pageable);
    }
}