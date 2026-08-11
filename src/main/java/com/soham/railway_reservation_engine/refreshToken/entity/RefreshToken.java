package com.soham.railway_reservation_engine.refreshToken.entity;


import com.soham.railway_reservation_engine.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.jdbc.datasource.init.UncategorizedScriptException;


import java.time.LocalDateTime;


@Entity
@Table(name ="refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;


    @Column(nullable = false , name = "revoked")
    private boolean revoked;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
