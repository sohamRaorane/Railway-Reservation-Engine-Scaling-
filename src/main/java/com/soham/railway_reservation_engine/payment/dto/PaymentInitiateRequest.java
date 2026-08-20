package com.soham.railway_reservation_engine.payment.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Initiate-payment request. Uses the PNR as the identifier because it is already exposed to the
 * client (unlike the internal numeric id), and validates it with Bean Validation annotations.
 */
public record PaymentInitiateRequest(
        @NotBlank(message = "Booking PNR is required.")
        String pnr
) {
}
