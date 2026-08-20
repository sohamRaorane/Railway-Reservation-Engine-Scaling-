package com.soham.railway_reservation_engine.refund.entity;

import com.soham.railway_reservation_engine.common.enums.RefundStatus;
import com.soham.railway_reservation_engine.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;



/**
 * Money returned to the customer after a cancellation.
 *
 * <p>One refund per payment (one-to-one). The refund amount is computed by
 * {@code ChargeCalculator} from the cancellation's time-window slab; the record tracks the
 * lifecycle (PENDING → PROCESSING → SUCCESS/FAILED) and the gateway's transaction id for
 * reconciliation. Like payments, amounts are {@code BigDecimal} — never floating point.
 */
@Entity
@Table(name = "refunds")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason")
    private String refundReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus;

    @Column(name = "refund_transaction_id", unique = true)
    private String refundTransactionId;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

}
