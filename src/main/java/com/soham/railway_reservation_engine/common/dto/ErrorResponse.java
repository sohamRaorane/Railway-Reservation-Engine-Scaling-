package com.soham.railway_reservation_engine.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;


/**
 * Standard error payload returned by {@code GlobalExceptionHandler}.
 *
 * <p>Designed with the <b>Principle of Least Privilege for API responses</b>: expose only
 * timestamp, HTTP status, reason phrase, message and the failing path — never stack traces or
 * internal state. The record's canonical constructor also removes the need for a
 * builder/constructor boilerplate (all fields are final).
 */
public record ErrorResponse(

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,

        int status,

        String error,

        String message,

        String path

) {
}