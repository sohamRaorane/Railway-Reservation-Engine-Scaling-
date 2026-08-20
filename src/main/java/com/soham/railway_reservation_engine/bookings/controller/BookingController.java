package com.soham.railway_reservation_engine.bookings.controller;


import com.soham.railway_reservation_engine.bookings.dto.BookingRequest;
import com.soham.railway_reservation_engine.bookings.dto.BookingResponse;
import com.soham.railway_reservation_engine.bookings.dto.CancellationResponse;
import com.soham.railway_reservation_engine.bookings.service.BookingCancellationService;
import com.soham.railway_reservation_engine.bookings.service.BookingService;
import com.soham.railway_reservation_engine.security.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP layer for booking operations.
 *
 * <p><b>Authentication:</b> the current user is injected via {@code @AuthenticationPrincipal}
 * as a {@link com.soham.railway_reservation_engine.security.service.CustomUserDetails} — set by
 * the JWT filter — so the service receives the caller's userId instead of trusting client input.
 *
 * <p><b>Idempotency:</b> creating a booking requires an {@code Idempotency-Key} header; re-sending
 * the same key returns the same booking rather than a duplicate.
 *
 * <p>Endpoints: {@code POST /api/v1/bookings}, {@code GET /api/v1/bookings/{pnr}},
 * {@code POST /api/v1/bookings/{pnr}/cancel}.
 */
@RestController
@RequestMapping("api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingCancellationService bookingCancellationService;

    @PostMapping
    public ResponseEntity<BookingResponse> bookTicket(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Valid
            @RequestBody
            BookingRequest bookingRequest,

            @RequestHeader("Idempotency-Key")
            String idempotencyKey
    ){
        BookingResponse response = bookingService.bookTicket(

                userDetails.getUserId(),

                bookingRequest,
                idempotencyKey


        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    //Get the booking details  by the pnr
    //Path Variable --> To pinpoint and identify a specific resource
    //Query Parameter (@RequestParam )--> To filter , sort , paginate ,
    // or provide optional context
    @GetMapping("/{pnr}")
    public ResponseEntity<BookingResponse> getBookingByPnr(
            @PathVariable String pnr
    ){
        BookingResponse response = bookingService.getBookingByPnr(pnr);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pnr}/cancel")
    public ResponseEntity<CancellationResponse> cancelBooking(
            @PathVariable String pnr
    ) {

        CancellationResponse response =
                bookingCancellationService.cancelBooking(
                        pnr
                );

        return ResponseEntity.ok(response);
    }

}
