package com.soham.railway_reservation_engine.auth.dto;

import com.soham.railway_reservation_engine.common.enums.Gender;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//Why the dtos are needed because the client sends the data in the json format and the spring requires the data
//in the object form so it is needed to convert the json data to object form and vice versa
public class RegisterRequest {

    private String name;

    private String email;

    private String password;

    private Gender gender;

    private LocalDate dateOfBirth;
}
