package com.soham.railway_reservation_engine.refreshToken.repository;

import com.soham.railway_reservation_engine.refreshToken.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


//Jpa --> responsible for CURD OPERATIONS ONLY

public interface RefreshTokenRepository  extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);


}
