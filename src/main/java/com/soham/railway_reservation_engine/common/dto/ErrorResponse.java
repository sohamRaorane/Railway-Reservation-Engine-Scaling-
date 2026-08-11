package com.soham.railway_reservation_engine.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;


//For only passing what you need avoiding the need to pass all the parameters in the constructor

public record ErrorResponse(

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,

        int status,

        String error,

        String message,

        String path

) {
}