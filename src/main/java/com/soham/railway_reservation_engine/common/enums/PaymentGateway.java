package com.soham.railway_reservation_engine.common.enums;

/**
 * Supported payment processors. The app currently integrates RAZORPAY only
 * (sandbox mode); STRIPE is reserved for future support.
 */
public enum PaymentGateway {
    RAZORPAY,
    STRIPE
}

