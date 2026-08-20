package com.soham.railway_reservation_engine.common.handler;

import com.soham.railway_reservation_engine.common.exception.BookingNotFoundException;
import com.soham.railway_reservation_engine.common.exception.PaymentAlreadyInitiatedException;
import com.soham.railway_reservation_engine.common.exception.PaymentNotAllowedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.web.ErrorResponse;
import com.soham.railway_reservation_engine.common.dto.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global, centralised exception interceptor ({@code @RestControllerAdvice}).
 *
 * <p><b>Advanced Spring concept — AOP-based exception translation:</b> instead of wrapping every
 * controller method in try/catch, one advice intercepts ALL exceptions thrown by any controller
 * and maps each exception <i>type</i> to a consistent HTTP status + {@code ErrorResponse} body.
 * Rules of this handler:
 * <ul>
 *   <li>{@code BookingNotFoundException} → 404 NOT_FOUND</li>
 *   <li>{@code PaymentAlreadyInitiatedException} → 409 CONFLICT (duplicate attempt)</li>
 *   <li>{@code PaymentNotAllowedException} → 400 BAD_REQUEST</li>
 *   <li>any other {@code Exception} → 500 INTERNAL_SERVER_ERROR (safety net — never leaks internals)</li>
 * </ul>
 *
 * <p>The response includes the request URI so clients can correlate the error with the call
 * that produced it.
 */
@Builder
@RestControllerAdvice
public class GlobalExceptionHandler {
    //Booking not found
    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookingNotFound(
            BookingNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );

    }

    //Payment already started
    @ExceptionHandler(PaymentAlreadyInitiatedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentAlreadyInitiated(
            PaymentAlreadyInitiatedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    //Payment Not allowed
    @ExceptionHandler(PaymentNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotAllowed(
            PaymentNotAllowedException ex,
            HttpServletRequest request

    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI() // the endpoint from which url you are  from where you are getting the error
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request.getRequestURI()
        );
    }
    //response entity--> entire banckend response sent to your client from the backend
    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path
    ) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );

        return ResponseEntity.status(status).body(response);

    }
}
