package com.soham.railway_reservation_engine.pnrStateHistory.entity;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.common.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Append-only audit trail of a booking's status changes.
 *
 * <p><b>Why this exists:</b> a booking's {@code bookingStatus} column only shows the CURRENT state.
 * For customer support, refund disputes and analytics, the full journey matters — when a PNR went
 * PENDING_PAYMENT → CONFIRMED, when it was cancelled, why. Each transition appends a row here.
 *
 * <p><b>Append-only invariant:</b> rows are never updated or deleted; {@code changedAt} is set by
 * Hibernate's {@code @CreationTimestamp} and the column is {@code updatable=false}, making the
 * history tamper-evident by construction.
 */
@Entity
@Table(name = "pnr_state_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PnrStateHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private BookingStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private BookingStatus currentStatus;

    @Column(name = "remarks")
    private String remarks;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;
}
