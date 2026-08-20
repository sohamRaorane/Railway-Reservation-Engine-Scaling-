package com.soham.railway_reservation_engine.refreshToken.repository;

import com.soham.railway_reservation_engine.refreshToken.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * Data access for refresh tokens. JPA repositories are strictly CRUD; validation and business
 * rules (expiry checks, revocation) live in {@code RefreshTokenService}.
 */
public interface RefreshTokenRepository  extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);


}
