package com.soham.railway_reservation_engine.user.entity;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.common.enums.Gender;
import com.soham.railway_reservation_engine.common.enums.KycStatus;
import com.soham.railway_reservation_engine.common.enums.Role;
import com.soham.railway_reservation_engine.refreshToken.entity.RefreshToken;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 100)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    private KycStatus kycStatus;


    //why i am not putting the creation time stamp --> to  follow the pattern flyways handles all the schema related work
    // and  hence we are not using the @Creation TimeStamp annotations
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<RefreshToken> refreshTokens = new ArrayList<>();


    @Column(name = "is_defence_personnel", nullable = false)
    private Boolean defencePersonnel = false;
}
