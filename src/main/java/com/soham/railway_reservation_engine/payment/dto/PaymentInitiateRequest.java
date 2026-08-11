package com.soham.railway_reservation_engine.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentInitiateRequest(
        //Pnr id is already exposed publivly so its better instead of booking id
        @NotBlank(message = "Booking PNR is required.")
        String pnr
) {

/*
   public String getPnr() {
        return null;
    }

 */
}
