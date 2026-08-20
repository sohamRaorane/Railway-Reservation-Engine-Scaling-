package com.soham.railway_reservation_engine.payment.repository;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;



import java.util.Optional;

/**
 * Data access for payments. Lookups by Razorpay ids let the webhook handler match an incoming
 * notification to the local payment row; the id columns are unique, so at most one match exists.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);

    //Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByRazorpayOrderId(
            String razorpayOrderId
    );

    Optional<Payment> findByRazorpayPaymentId(
            String razorpayPaymentId
    );
}
