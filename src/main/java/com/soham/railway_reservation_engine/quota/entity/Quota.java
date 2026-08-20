package com.soham.railway_reservation_engine.quota.entity;


import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.waitlist.entity.Waitlist;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A quota (GN, LD, SS, DEF, TQ) — the customer-class rule set applied to a booking.
 *
 * <p><b>Terminology:</b> quotas partition a train's berths into pools with distinct eligibility
 * rules. The {@code code} is the short identifier sent by the client (e.g. {@code GN}), and is
 * unique — so the app can look a quota up by code without scanning. Seats under a quota are
 * tracked per coach via {@code QuotaSeatAllocation}; waiting customers queue via {@code Waitlist}.
 *
 * <p>This is a lookup/reference entity (metadata): it has no mutable counters. Availability
 * counters live on the schedule-specific {@code QuotaReservationPool} instead, because they vary
 * per train-date rather than being fixed properties of the quota itself.
 */
@Entity
@Table(name = "quotas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,
            unique = true,
            length = 10)
    private String code;


    @Column(nullable = false,length = 100)
    private String name;

    @OneToMany(mappedBy = "quota", fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "quota", fetch = FetchType.LAZY)
    private List<QuotaSeatAllocation> quotaSeatAllocations = new ArrayList<>();

    @OneToMany(mappedBy = "quota", fetch = FetchType.LAZY)
    private List<Waitlist> waitlists = new ArrayList<>();
}
