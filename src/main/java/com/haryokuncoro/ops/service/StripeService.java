package com.haryokuncoro.ops.service;

import com.haryokuncoro.ops.dto.enums.PayoutStatus;
import com.haryokuncoro.ops.dto.enums.TransactionType;
import com.haryokuncoro.ops.entity.PayoutTransaction;
import com.haryokuncoro.ops.exception.NotFoundException;
import com.haryokuncoro.ops.repository.PayoutRepository;
import com.haryokuncoro.ops.repository.PayoutTransactionRepository;
import com.haryokuncoro.ops.stripe.StripeKeyResolver;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Payout;
import com.stripe.model.Transfer;
import com.stripe.net.ApiResource;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PayoutCreateParams;
import com.stripe.param.TransferCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

import static com.haryokuncoro.ops.dto.enums.PayoutStatus.PAID;
import static com.haryokuncoro.ops.dto.enums.PayoutStatus.TRANSFERRED;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class StripeService {

    @Value("${stripe.skipSignatureCheck}")
    private boolean skipSignatureCheck;

    @Value("${demo.mode}")
    private boolean demoMode;

    private final PayoutTransactionRepository payoutTransactionRepository;
    private final PayoutRepository payoutRepository;
    private final StripeKeyResolver stripeKeyResolver;

    private String mockTransferId() {
        return "tr_demo_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    private String mockPayoutId() {
        return "po_demo_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    private BigDecimal toDecimalAmount(Long amount) {
        return BigDecimal.valueOf(amount)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public String transfer(com.haryokuncoro.ops.entity.Payout payout,
                           Long amount,
                           String currency,
                           String destinationAccount) throws StripeException {

        BigDecimal dollarAmount = toDecimalAmount(amount);

        if (demoMode) {
            String mockId = mockTransferId();

            log.info("Demo mode: skipping Stripe transfer. payoutId={}, mockTransferId={}",
                    payout.getId(), mockId);

            payoutTransactionRepository.save(PayoutTransaction.builder()
                    .transactionType(TransactionType.TRANSFER)
                    .payout(payout)
                    .referenceId(mockId)
                    .amount(dollarAmount)
                    .status(TRANSFERRED)
                    .metadata("Demo mode mock transfer")
                    .build());

            return mockId;
        }

        String apiKey = stripeKeyResolver.resolveApiKey(currency);

        RequestOptions options = RequestOptions.builder()
                .setApiKey(apiKey)
                .setIdempotencyKey("transfer-" + payout.getId() + "-" + destinationAccount + "-" + amount)
                .build();

        TransferCreateParams params = TransferCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setDestination(destinationAccount)
                .build();

        try {
            Transfer transfer = Transfer.create(params, options);

            payoutTransactionRepository.save(PayoutTransaction.builder()
                    .transactionType(TransactionType.TRANSFER)
                    .payout(payout)
                    .referenceId(transfer.getId())
                    .amount(dollarAmount)
                    .status(TRANSFERRED)
                    .build());

            return transfer.getId();

        } catch (StripeException e) {
            log.error("Transfer failed. payoutId={}, destination={}, amount={}, code={}, message={}",
                    payout.getId(), destinationAccount, amount, e.getCode(), e.getMessage(), e);

            payoutTransactionRepository.save(PayoutTransaction.builder()
                    .transactionType(TransactionType.TRANSFER)
                    .payout(payout)
                    .amount(dollarAmount)
                    .status(PayoutStatus.FAILED)
                    .metadata(e.getMessage())
                    .build());

            throw e;
        }
    }

    public String payout(com.haryokuncoro.ops.entity.Payout payoutEntity,
                         Long amount,
                         String currency,
                         String connectedAccountId) throws StripeException {

        BigDecimal dollarAmount = toDecimalAmount(amount);

        if (demoMode) {
            String mockId = mockPayoutId();

            log.info("Demo mode: skipping Stripe payout. payoutId={}, mockPayoutId={}",
                    payoutEntity.getId(), mockId);

            payoutTransactionRepository.save(PayoutTransaction.builder()
                    .transactionType(TransactionType.PAYOUT)
                    .payout(payoutEntity)
                    .referenceId(mockId)
                    .amount(dollarAmount)
                    .status(PAID)
                    .metadata("Demo mode mock payout")
                    .build());

            return mockId;
        }

        String apiKey = stripeKeyResolver.resolveApiKey(currency);

        RequestOptions options = RequestOptions.builder()
                .setApiKey(apiKey)
                .setStripeAccount(connectedAccountId)
                .setIdempotencyKey("payout-" + payoutEntity.getId() + "-" + connectedAccountId + "-" + amount)
                .build();

        PayoutCreateParams params = PayoutCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .build();

        try {
            Payout stripePayout = Payout.create(params, options);

            payoutTransactionRepository.save(PayoutTransaction.builder()
                    .transactionType(TransactionType.PAYOUT)
                    .payout(payoutEntity)
                    .referenceId(stripePayout.getId())
                    .amount(dollarAmount)
                    .status(PAID)
                    .build());

            return stripePayout.getId();

        } catch (StripeException e) {
            log.error("Payout failed. payoutEntityId={}, account={}, amount={}, code={}, message={}",
                    payoutEntity.getId(), connectedAccountId, amount, e.getCode(), e.getMessage(), e);

            payoutTransactionRepository.save(PayoutTransaction.builder()
                    .transactionType(TransactionType.PAYOUT)
                    .payout(payoutEntity)
                    .amount(dollarAmount)
                    .status(PayoutStatus.FAILED)
                    .metadata(e.getMessage())
                    .build());

            throw e;
        }
    }

    public void cancelPayout(com.haryokuncoro.ops.entity.Payout payoutEntity) throws StripeException {

        if (demoMode) {
            log.info("Demo mode: skipping Stripe payout cancellation. payoutId={}", payoutEntity.getId());
            payoutTransactionRepository.save(PayoutTransaction.builder()
                    .transactionType(TransactionType.PAYOUT)
                    .payout(payoutEntity)
                    .referenceId(payoutEntity.getStripePayoutId())
                    .amount(payoutEntity.getPayoutAmount())
                    .status(PayoutStatus.CANCELLED)
                    .metadata("Demo mode mock cancellation")
                    .build());

            return;
        }

        String apiKey = stripeKeyResolver.resolveApiKey(payoutEntity.getCurrency());
        String connectedAccountId = payoutEntity.getMerchant().getStripeAccountId();

        RequestOptions options = RequestOptions.builder()
                .setApiKey(apiKey)
                .setStripeAccount(connectedAccountId)
                .build();

        try {
            Payout stripePayout = Payout.retrieve(payoutEntity.getStripePayoutId(), options);
            stripePayout.cancel();

            payoutTransactionRepository.save(PayoutTransaction.builder()
                    .transactionType(TransactionType.PAYOUT)
                    .payout(payoutEntity)
                    .referenceId(stripePayout.getId())
                    .amount(payoutEntity.getPayoutAmount())
                    .status(PayoutStatus.CANCELLED)
                    .build());

        } catch (StripeException e) {
            log.error("Failed to cancel Payout. payoutEntityId={}, account={}, amount={}, code={}, message={}",
                    payoutEntity.getId(), connectedAccountId, payoutEntity.getPayoutAmount(), e.getCode(), e.getMessage(), e);

            throw e;
        }
    }

    public void handleWebhook(String payload, String signature, String webhookSecret) {

        Event event;

        if (skipSignatureCheck) {
            event = ApiResource.GSON.fromJson(payload, Event.class);
        } else {
            try {
                event = Webhook.constructEvent(payload, signature, webhookSecret);
            } catch (SignatureVerificationException ex) {
                log.error("Invalid webhook signature", ex);
                throw new RuntimeException("Invalid webhook signature", ex);
            }
        }

        switch (event.getType()) {
            case "payout.paid" -> handlePayoutPaid(event);
            case "payout.failed" -> handlePayoutFailed(event);
            default -> log.info("Ignoring event {}", event.getType());
        }
    }

    private void handlePayoutPaid(Event event) {
        handlePayoutStatusUpdate(event, PayoutStatus.PAID);
    }

    private void handlePayoutFailed(Event event) {
        handlePayoutStatusUpdate(event, PayoutStatus.FAILED);
    }

    private void handlePayoutStatusUpdate(Event event, PayoutStatus status) {
        Payout stripePayout;

        try {
            stripePayout = (Payout) event.getDataObjectDeserializer().deserializeUnsafe();
        } catch (Exception e) {
            log.error("Failed to deserialize payout object", e);
            return;
        }

        com.haryokuncoro.ops.entity.Payout payout = payoutRepository
                .findByStripePayoutId(stripePayout.getId())
                .orElseThrow(() -> new NotFoundException("Payout not found"));

        payout.setStatus(status);
        payout.setPayoutDate(Instant.now());

        payoutRepository.save(payout);
    }
}