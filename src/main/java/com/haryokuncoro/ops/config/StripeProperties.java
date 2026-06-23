package com.haryokuncoro.ops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stripe")
public record StripeProperties(
        String apiKeySG,
        String apiKeyMY,
        String apiKeyID,
        boolean mockEnabled,
        String mockBaseUrl
) {}