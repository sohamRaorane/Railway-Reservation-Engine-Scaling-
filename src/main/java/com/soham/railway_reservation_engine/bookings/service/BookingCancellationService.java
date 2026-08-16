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
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.quotaSeatAllocation.repository.QuotaSeatAllocationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    private final BookingEventProducer bookingEventProducer;



    public CancellationResponse cancelBooking(String pnr){
        //Find booking
        Booking booking = bookingRepository.findByPnr(pnr).orElseThrow(() ->new RuntimeException("Booking not found for PNR: " + pnr));

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
                                        booking.getQuota().getId()
                                )
                        );
                    }
                }
        );


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
