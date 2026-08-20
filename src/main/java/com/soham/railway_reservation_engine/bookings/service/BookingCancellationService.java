package com.soham.railway_reservation_engine.bookings.service;

import com.soham.railway_reservation_engine.bookings.dto.CancellationResponse;
import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.bookings.event.BookingCancelledEvent;
import com.soham.railway_reservation_engine.bookings.repository.BookingRepository;
import com.soham.railway_reservation_engine.cancellation.service.ChargeCalculator;
import com.soham.railway_reservation_engine.common.enums.BookingStatus;
import com.soham.railway_reservation_engine.common.enums.PassengerStatus;
import com.soham.railway_reservation_engine.common.enums.RefundStatus;
import com.soham.railway_reservation_engine.kafka.producer.BookingEventProducer;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.payment.entity.Payment;
import com.soham.railway_reservation_engine.payment.repository.PaymentRepository;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.quotaSeatAllocation.repository.QuotaSeatAllocationRepository;
import com.soham.railway_reservation_engine.refund.entity.Refund;
import com.soham.railway_reservation_engine.refund.repository.RefundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Handles cancellation of an entire booking (all passengers).
 *
 * <p><b>Business flow:</b>
 * <ol>
 *   <li>Looks up the booking by PNR.</li>
 *   <li>Computes the refund via {@link com.soham.railway_reservation_engine.cancellation.service.ChargeCalculator}
 *       — a time-window slab rule (75% refund &gt;48h, 50% 12–48h, 0% &lt;12h before departure).</li>
 *   <li>Marks the booking and every passenger {@code CANCELLED} and releases confirmed seats back
 *       into their quota allocation (increments {@code availableSeats}).</li>
 *   <li>Publishes a {@code booking.cancelled} Kafka event <b>after commit</b>, which the
 *       {@code BookingCancelledConsumer} consumes to trigger waitlist/RAC promotion asynchronously —
 *       decoupling cancellation from promotion so the caller does not wait for seat re-allocation.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Transactional

public class BookingCancellationService {
    private final BookingRepository bookingRepository;
    private final QuotaSeatAllocationRepository quotaSeatAllocationRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final ChargeCalculator chargeCalculator;
    private static final Logger log = LoggerFactory.getLogger(BookingCancellationService.class);
   // private final WaitlistPromotionService waitlistPromotionService;
    //we have commented this out because we no longer want the cancellation service
    //to know about how the promotion works

    private final BookingEventProducer  bookingEventProducer;



    public CancellationResponse cancelBooking(String pnr){
        //Find booking
        Booking booking = bookingRepository.findByPnr(pnr).orElseThrow(() ->new RuntimeException("Booking not found for PNR: " + pnr));

        log.info(
                "Starting booking cancellation: bookingId= {}  , pnr ={}",
                booking.getId(),
                booking.getPnr()
        );


        //Calculate the refund
        LocalDateTime departureDateTime =
                LocalDateTime.of(
                        booking.getSchedule().getJourneyDate(),
                        booking.getSchedule().getDepartureTime()
                );
        BigDecimal refundAmount = chargeCalculator.calculateRefund(
                booking.getTotalFare(),
               departureDateTime,
                LocalDateTime.now()
        );

        log.info(
                "Refund amount calculated: pnr={}, refundAmount={}",
                booking.getPnr(),
                refundAmount
        );

        //Persist an audit trail for the refund — the Refund entity/table exist but were never
        //written here, so cancellations had no record of the money returned.
        paymentRepository.findByBooking(booking).ifPresent(payment ->
                refundRepository.save(
                        Refund.builder()
                                .payment(payment)
                                .refundAmount(refundAmount)
                                .refundReason("Booking cancelled: " + booking.getPnr())
                                .refundStatus(RefundStatus.PENDING)
                                .build()
                )
        );

        //cancel booking
        booking.setBookingStatus(BookingStatus.CANCELLED);

        //cancel every passenger and count the confirmed seats released so the consumer
        //can run one promotion per freed seat (see BookingCancelledEvent.freedSeatCount)
        int freedSeatCount = 0;

        for(Passenger passenger : booking.getPassengers()){
            passenger.setPassengerStatus(PassengerStatus.CANCELLED);
            //then release the confirm seat only
            if(passenger.getSeat() != null){
                releaseSeat(booking , passenger);
                freedSeatCount++;
            }
        }
        log.info(
                "Booking cancellation committed , publishing Kafka event: bookingId= {} , pnr ={}, freedSeatCount={}",
                booking.getId(),
                booking.getPnr(),
                freedSeatCount
        );

        // Capture for the afterCommit callback (anonymous class requires effectively-final variables)
        final int seatsToPromote = freedSeatCount;


        // Publish after the DB transaction commits to avoid consumers observing uncommitted/rolled-back state
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        bookingEventProducer.publishBookingCancelled(
                                new BookingCancelledEvent(
                                        booking.getId(),
                                        booking.getPnr(),
                                        booking.getSchedule().getId(),
                                        booking.getQuota().getId(),
                                        seatsToPromote,
                                        MDC.get("correlationId")
                                )
                        );
                    }
                }
        );


        //trigger the promotion logic

        return new CancellationResponse(

                booking.getPnr(),

                booking.getBookingStatus(),

                refundAmount,

                "Booking cancelled successfully."

        );
    }

    private void releaseSeat(Booking booking, Passenger passenger) {
        QuotaSeatAllocation allocation =
                quotaSeatAllocationRepository
                        .findByScheduleAndCoachAndQuota(
                                booking.getSchedule(),
                                passenger.getSeat().getCoach(),
                                booking.getQuota()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Quota allocation not found."
                                )
                        );

        allocation.setAvailableSeats(
                allocation.getAvailableSeats() + 1
        );


    }


}
