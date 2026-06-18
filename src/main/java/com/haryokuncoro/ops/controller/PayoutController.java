package com.haryokuncoro.ops.controller;

import com.haryokuncoro.ops.dto.ApiResponse;
import com.haryokuncoro.ops.dto.CreateOrderRequest;
import com.haryokuncoro.ops.dto.CreatePayoutRequest;
import com.haryokuncoro.ops.service.PayoutService;
import com.haryokuncoro.ops.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
public class PayoutController {
    private final PayoutService payoutService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> create(@RequestBody CreatePayoutRequest request) {
        payoutService.createPayout(request);
        return ResponseEntity.ok(
                ResponseUtil.success("finished payout", null)
        );
    }
}
