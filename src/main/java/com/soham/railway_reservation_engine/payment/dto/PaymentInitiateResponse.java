package com.soham.railway_reservation_engine.payment.dto;

import java.math.BigDecimal;

public record PaymentInitiateResponse(

        String orderId,

        String keyId,

        BigDecimal amount,

        String currency,

        String bookingPnr

) {
}