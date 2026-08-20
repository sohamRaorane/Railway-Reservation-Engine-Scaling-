package com.soham.railway_reservation_engine.common.enums;

/**
 * Instruments a customer can pay with. The Razorpay flow currently records ONLINE;
 * the UPI/card values are reserved for richer instrument tracking later.
 */
public enum PaymentMethod {
    UPI,
    CREDIT_CARD,
    DEBIT_CARD,
    NET_BANKING,
    WALLET,
    ONLINE
}
