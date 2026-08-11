package com.soham.railway_reservation_engine.auth.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResponse {
    private String accessToken;
}
