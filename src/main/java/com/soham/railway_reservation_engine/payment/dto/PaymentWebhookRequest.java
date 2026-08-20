package com.soham.railway_reservation_engine.payment.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes the JSON body Razorpay posts to the webhook endpoint after a payment completes.
 *
 * <p>Currently informational: the actual handler reads the raw payload string (for signature
 * verification) and parses it as JSON directly. The {@code @JsonProperty} names show the
 * snake_case contract Razorpay sends, which differs from Java's camelCase convention.
 */
public record PaymentWebhookRequest(

        //it automatically maps the incoming JSON fields to your java fields
        @JsonProperty("razorpay_order_id")
        String razorpayOrderId,

        @JsonProperty("razorpay_payment_id")
        String razorpayPaymentId,

        @JsonProperty("razorpay_signature")
        String razorpaySignature

) {
}