package com.soham.railway_reservation_engine.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    //This  will be the message that will be returned to the user after successful registration
    private String message;
}
