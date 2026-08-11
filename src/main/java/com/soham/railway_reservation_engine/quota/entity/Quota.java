package com.soham.railway_reservation_engine.quota.entity;


import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.waitlist.entity.Waitlist;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
