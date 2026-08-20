package com.soham.railway_reservation_engine.payment.dto;

import java.math.BigDecimal;

/**
 * Response returned after an order is created. Contains everything the frontend needs to open the
 * Razorpay checkout: the order id, the merchant's public key id, and the amount/currency context.
 */
public record PaymentInitiateResponse(

        String orderId,

        String keyId,

        BigDecimal amount,

        String currency,

        String bookingPnr

) {
}