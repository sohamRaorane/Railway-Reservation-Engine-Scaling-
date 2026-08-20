package com.soham.railway_reservation_engine.refund.repository;

import com.soham.railway_reservation_engine.payment.entity.Payment;
import com.soham.railway_reservation_engine.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Data access for refunds; lookups by payment (one per payment) or by the gateway's
 * refund transaction id (unique) for idempotent reconciliation.
 */
public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByPayment(Payment payment);

    Optional<Refund> findByRefundTransactionId(String refundTransactionId);

}