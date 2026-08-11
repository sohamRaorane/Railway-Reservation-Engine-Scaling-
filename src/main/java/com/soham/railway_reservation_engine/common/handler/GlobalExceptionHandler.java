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

@Builder
@RestControllerAdvice// creates a global , centralized interceptor for your application's backend
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
