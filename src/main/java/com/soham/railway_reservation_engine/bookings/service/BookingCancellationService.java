package com.soham.railway_reservation_engine.bookings.service;

import com.soham.railway_reservation_engine.bookings.dto.CancellationResponse;
import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.bookings.event.BookingCancelledEvent;
import com.soham.railway_reservation_engine.bookings.repository.BookingRepository;
import com.soham.railway_reservation_engine.cancellation.service.ChargeCalculator;
import com.soham.railway_reservation_engine.common.enums.BookingStatus;
import com.soham.railway_reservation_engine.common.enums.PassengerStatus;
import com.soham.railway_reservation_engine.kafka.producer.BookingEventProducer;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.passenger.repository.PassengerRepository;
import com.soham.railway_reservation_engine.payment.repository.PaymentRepository;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.quotaSeatAllocation.repository.QuotaSeatAllocationRepository;
import com.soham.railway_reservation_engine.seat.repository.SeatRepository;
import com.soham.railway_reservation_engine.waitlist.service.WaitlistPromotionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional

public class BookingCancellationService {
    private final BookingRepository bookingRepository;
    private final QuotaSeatAllocationRepository quotaSeatAllocationRepository;
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
        BigDecimal refund = chargeCalculator.calculateRefund(
                booking.getTotalFare(),
               departureDateTime,
                LocalDateTime.now()
        );

        //debbugging
        System.out.println("Refund Amount : " + refund);


        //cancel booking
        booking.setBookingStatus(BookingStatus.CANCELLED);

        //cancel every passneger

        for(Passenger passenger : booking.getPassengers()){
            passenger.setPassengerStatus(PassengerStatus.CANCELLED);
            //then release the confirm seat only
            if(passenger.getSeat() != null){
                releaseSeat(booking , passenger);
            }
        }
        log.info(
                "Booking cancellation committed , publishing Kafka event: bookingId= {} , pnr ={}",
                booking.getId(),
                booking.getPnr()
        );


        //publish the kafka event
        BookingCancelledEvent event = new BookingCancelledEvent(
                booking.getId(),
                booking.getPnr(),
                booking.getSchedule().getId(),
                booking.getQuota().getId()
        );
        bookingEventProducer.publishBookingCancelled(event);


        //trigger the promotion logic

        return new CancellationResponse(

                booking.getPnr(),

                booking.getBookingStatus(),

                refund,

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
