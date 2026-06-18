package com.haryokuncoro.ops.service;

import com.haryokuncoro.ops.dto.CreatePayoutRequest;
import com.haryokuncoro.ops.dto.PayoutJobEvent;
import com.haryokuncoro.ops.event.producer.PayoutEventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service @Slf4j
@RequiredArgsConstructor
public class PayoutService {
    private final StripeService stripeService;
    private final PayoutEventPublisher publisher;

    @Transactional
    public void createPayout(CreatePayoutRequest request){
        log.info("initiate payout {}", request);
        stripeService.payout();
    }

    @Transactional
    public void publishPayoutJobs(CreatePayoutRequest request) {
        log.info("publishPayoutJobs -> gather order data");
        List<UUID> merchants = new ArrayList<>();
        merchants.add(UUID.randomUUID());
        merchants.add(UUID.randomUUID());
        merchants.add(UUID.randomUUID());
        merchants.add(UUID.randomUUID());
        merchants.add(UUID.randomUUID());
        merchants.add(UUID.randomUUID());
        merchants.add(UUID.randomUUID());
        merchants.add(UUID.randomUUID());
        for(UUID merchantId : merchants){
            UUID id = UUID.randomUUID();
            PayoutJobEvent event = PayoutJobEvent.builder()
                    .eventId(id)
                    .merchantId(merchantId)
                    .type(request.getType())
                    .billingCycle(request.getBillingCycle())
                    .build();
            publisher.publish(event);
        }

    }

    @Transactional
    public void handlePayoutJobs(PayoutJobEvent event){
        log.info("handle payout jobs {}", event);
        createPayout(CreatePayoutRequest.builder()
                .merchantId(event.getMerchantId())
                .billingCycle(event.getBillingCycle())
                .type(event.getType())
                .build());
    }

}
