package com.haryokuncoro.ops.controller;

import com.haryokuncoro.ops.service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    private final StripeService stripeService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signature) {

        stripeService.handleWebhook(
                payload,
                signature,
                webhookSecret
        );

        return ResponseEntity.ok().build();
    }
}
