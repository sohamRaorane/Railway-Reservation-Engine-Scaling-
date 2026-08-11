package com.soham.railway_reservation_engine.payment.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "razorpay")//binds external configuraion properties directly in to a structured java object
public class RazorpayProperties {
        private String keyId;
        private String keySecret;
        private String webhookSecret;

}
