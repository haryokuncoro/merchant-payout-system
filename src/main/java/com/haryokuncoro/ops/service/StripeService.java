package com.haryokuncoro.ops.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Payout;
import com.stripe.model.Transfer;
import com.stripe.param.PayoutCreateParams;
import com.stripe.param.TransferCreateParams;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service @Slf4j
public class StripeService {
    public String transfer(
            Long amount,
            String currency,
            String destinationAccount
    ) throws StripeException {

        TransferCreateParams params =
                TransferCreateParams.builder()
                        .setAmount(amount)
                        .setCurrency(currency)
                        .setDestination(destinationAccount)
                        .build();

        Transfer transfer = Transfer.create(params);

        log.info(
                "Transfer created. id={}, destination={}, amount={}",
                transfer.getId(),
                destinationAccount,
                amount
        );

        return transfer.getId();
    }

    @Transactional
    public String payout(
            Long amount,
            String currency,
            String connectedAccountId
    ) throws StripeException {

        PayoutCreateParams params =
                PayoutCreateParams.builder()
                        .setAmount(amount)
                        .setCurrency(currency)
                        .build();

        Payout payout = Payout.create(
                params,
                com.stripe.net.RequestOptions.builder()
                        .setStripeAccount(connectedAccountId)
                        .build()
        );

        log.info(
                "Payout created. id={}, account={}, amount={}",
                payout.getId(),
                connectedAccountId,
                amount
        );

        return payout.getId();
    }
}
