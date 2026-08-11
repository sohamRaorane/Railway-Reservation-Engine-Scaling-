package com.soham.railway_reservation_engine.payment.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

//razorpay notifies our backend after payment is completed,
// we need to handle that notification and update the payment status in our system
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