package com.soham.railway_reservation_engine.refreshToken.entity;


import com.soham.railway_reservation_engine.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.jdbc.datasource.init.UncategorizedScriptException;


import java.time.LocalDateTime;


/**
 * Persisted refresh token — the long-lived credential exchanged for new access tokens.
 *
 * <p><b>Why not just JWT access tokens?</b> Access tokens are short-lived (minutes) to limit the
 * damage if leaked. The client proves its identity with a refresh token to mint new access tokens
 * without re-entering credentials. Unlike JWTs, refresh tokens are opaque random strings stored in
 * the DB, so they can be revoked server-side (the {@code revoked} flag) — revocation of a JWT is
 * impossible without a blacklist, which is why this entity exists.
 *
 * <p>{@code expiryDate} encodes validity; {@code revoked} kills a token early (e.g. on logout).
 */
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
