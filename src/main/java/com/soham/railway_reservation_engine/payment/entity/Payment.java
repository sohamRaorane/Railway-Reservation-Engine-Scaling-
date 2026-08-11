package com.soham.railway_reservation_engine.payment.entity;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.common.enums.PaymentGateway;
import com.soham.railway_reservation_engine.common.enums.PaymentMethod;
import com.soham.railway_reservation_engine.common.enums.PaymentStatus;
import com.soham.railway_reservation_engine.refund.entity.Refund;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;


    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToOne(mappedBy = "payment", fetch = FetchType.LAZY)
    private Refund refund;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentGateway gateway;

    @Column(name = "razorpay_order_id", unique = true)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", unique = true)
    private String razorpayPaymentId;

    public void setTransactionId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }
}
