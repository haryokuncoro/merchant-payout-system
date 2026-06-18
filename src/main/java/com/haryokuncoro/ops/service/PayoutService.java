package com.haryokuncoro.ops.service;

import com.haryokuncoro.ops.dto.CreatePayoutRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class PayoutService {
    private final StripeService stripeService;

    @Transactional
    public void createPayout(CreatePayoutRequest request){
        log.info("initiate payout {}", request);
        stripeService.payout();
    }

    @Transactional
    public void initAllPayout(CreatePayoutRequest request){
        log.info("initiate payout {}", request);
        stripeService.payout();
    }

}
