package com.soham.railway_reservation_engine.refreshToken.service;

import com.soham.railway_reservation_engine.refreshToken.entity.RefreshToken;
import com.soham.railway_reservation_engine.refreshToken.repository.RefreshTokenRepository;
import com.soham.railway_reservation_engine.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/*
Functions:
generate the refresh token
save refresh token
validate refresh token
revoke refresh token
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS= 7; // 7 days

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(generateToken())
                .expiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
    public RefreshToken  findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }
    public boolean isExpired(RefreshToken refreshToken) {
        return refreshToken.getExpiryDate().isBefore(LocalDateTime.now());
    }
    public void revokeRefreshToken(RefreshToken refreshToken) {
        //to offically cancel something so that it is no longer valid or effective --> revoke
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    private String generateToken() {
        return java.util.UUID.randomUUID().toString();
    }
}
