package com.soham.railway_reservation_engine.payment.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed view of the {@code razorpay.*} configuration block (key id, key secret, webhook secret).
 *
 * <p><b>Advanced Spring concept — configuration properties binding:</b> Spring Boot reads the
 * matching keys from {@code application.yml} and auto-populates this POJO, giving compile-time
 * typing instead of scattered {@code @Value} string lookups. Secrets are supplied via environment
 * variables in production, never committed to the repository.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "razorpay")
public class RazorpayProperties {
        private String keyId;
        private String keySecret;
        private String webhookSecret;

}
