package com.soham.railway_reservation_engine.auth.dto;


import lombok.*;
import org.springframework.scheduling.support.SimpleTriggerContext;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
    private String refreshToken;

}
