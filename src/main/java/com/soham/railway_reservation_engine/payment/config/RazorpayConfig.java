package com.soham.railway_reservation_engine.payment.config;


import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
