package com.soham.railway_reservation_engine.bookings.dto;

import com.soham.railway_reservation_engine.common.enums.BerthPreference;
import com.soham.railway_reservation_engine.common.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


public record PassengerRequest(
        @NotBlank(message = "Passenger name is required ")
        String name,

        @NotNull(message = "Passenger age is required ")
        @Min(value = 1, message = "Passenger age must be a positive integer ")
        @Max(value = 125, message = "Passenger age must be less than or equal to 125 ")

        Integer age,

        @NotNull(message = "Passenger gender is required ")
        Gender gender,


        //No preference do not contains any syntax becuase --> it simply means i am okay with any kind of the berth

        BerthPreference berthPreference
) {
        public int getAge() {
            return age;
        }

        public Gender getGender() {
            return gender;
        }
}
