package com.soham.railway_reservation_engine.payment.config;


import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the {@code RazorpayClient} used for creating payment orders.
 *
 * <p>The client is instantiated once as a Spring bean and injected wherever payments are handled.
 * Credentials are externalised via {@link RazorpayProperties} (bound from {@code razorpay.*} in
 * application properties) rather than hard-coded, so the same build runs in dev (sandbox keys)
 * and prod (live keys) unchanged.
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RazorpayProperties.class)
public class RazorpayConfig {
    private final RazorpayProperties razorpayProperties;
    @Bean
    public RazorpayClient razorpayClient() throws Exception {
        return new RazorpayClient(razorpayProperties.getKeyId(),
                razorpayProperties.getKeySecret());
}
}
