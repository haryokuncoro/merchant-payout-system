package com.haryokuncoro.ops.service;

import com.haryokuncoro.ops.stripe.StripeKeyResolver;
import com.stripe.exception.StripeException;
import com.stripe.model.Payout;
import com.stripe.model.Transfer;
import com.stripe.net.RequestOptions;
import com.stripe.param.PayoutCreateParams;
import com.stripe.param.TransferCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service @Slf4j
@RequiredArgsConstructor
public class StripeService {
    private final StripeKeyResolver stripeKeyResolver;

    public String transfer(Long amount, String currency, String destinationAccount) throws StripeException {
        String apiKey = stripeKeyResolver.resolveApiKey(currency);
        RequestOptions options = RequestOptions.builder()
                .setApiKey(apiKey)
                .build();

        TransferCreateParams params =
                TransferCreateParams.builder()
                        .setAmount(amount)
                        .setCurrency(currency)
                        .setDestination(destinationAccount)
                        .build();

        Transfer transfer = Transfer.create(params,  options);

        log.info(
                "Transfer created. id={}, destination={}, amount={}",
                transfer.getId(),
                destinationAccount,
                amount
        );

        return transfer.getId();
    }

    @Transactional
    public String payout(Long amount, String currency, String connectedAccountId) throws StripeException {
        String apiKey = stripeKeyResolver.resolveApiKey(currency);
        RequestOptions options = RequestOptions.builder()
                .setApiKey(apiKey)
                .build();

        PayoutCreateParams params = PayoutCreateParams.builder()
                        .setAmount(amount)
                        .setCurrency(currency)
                        .build();

        Payout payout = Payout.create(params, options);

        log.info(
                "Payout created. id={}, account={}, amount={}",
                payout.getId(),
                connectedAccountId,
                amount
        );

        return payout.getId();
    }
}
