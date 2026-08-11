package com.soham.railway_reservation_engine.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LoginResponse {

    private String accessToken;
    private String refreshToken;

}