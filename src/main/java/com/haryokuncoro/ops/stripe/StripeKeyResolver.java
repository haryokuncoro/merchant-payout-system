package com.haryokuncoro.ops.stripe;

import com.haryokuncoro.ops.config.StripeProperties;
import org.springframework.stereotype.Component;

@Component
public class StripeKeyResolver {
    private final StripeProperties properties;

    public StripeKeyResolver(StripeProperties properties) {
        this.properties = properties;
    }

    public String resolveApiKey(String currency) {
        return switch (currency) {
            case "SGD" -> properties.apiKeySG();
            case "MYR" -> properties.apiKeyMY();
            case "IDR" -> properties.apiKeyID();
            default -> throw new IllegalStateException("Unexpected value: " + currency);
        };
    }
}