package com.soham.railway_reservation_engine.bookings.service;


//import java.security.Provider;

import com.soham.railway_reservation_engine.bookings.dto.BookingRequest;
import com.soham.railway_reservation_engine.bookings.dto.BookingResponse;
import com.soham.railway_reservation_engine.bookings.dto.PassengerRequest;
import com.soham.railway_reservation_engine.bookings.dto.PassengerResponse;
import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.bookings.repository.BookingRepository;
import com.soham.railway_reservation_engine.bookings.validator.QuotaEligibilityValidator;
import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.common.enums.BookingStatus;
import com.soham.railway_reservation_engine.common.enums.PassengerStatus;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.passenger.repository.PassengerRepository;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quota.repository.QuotaRepository;
import com.soham.railway_reservation_engine.quotaReservationPool.repository.QuotaReservationPoolRepository;
import com.soham.railway_reservation_engine.quotaReservationPool.entity.QuotaReservationPool;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.quotaSeatAllocation.repository.QuotaSeatAllocationRepository;
import com.soham.railway_reservation_engine.rac.entity.Rac;
import com.soham.railway_reservation_engine.rac.repository.RacRepository;
import com.soham.railway_reservation_engine.rac.service.RacService;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.schedule.repository.ScheduleRepository;
import com.soham.railway_reservation_engine.seat.entity.Seat;
import com.soham.railway_reservation_engine.seat.service.SeatHoldService;
import com.soham.railway_reservation_engine.train.entity.Train;
import com.soham.railway_reservation_engine.train.repository.TrainRepository;
import com.soham.railway_reservation_engine.user.entity.User;
import com.soham.railway_reservation_engine.user.repository.UserRepository;
import com.soham.railway_reservation_engine.waitlist.entity.Waitlist;
import com.soham.railway_reservation_engine.waitlist.repository.WaitlistRepository;
import com.soham.railway_reservation_engine.waitlist.service.WaitlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {
    /*
      This class will contain the business logic for handling booking operations.
     Part  1: Service skeleton + create booking
     Part2 : Passenger loop + seat allocation + quota update
     Part 3: Save everything + build booking resposne

     */

    //---- Part 1 (Preparation Phase ) ----------
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;
    private final TrainRepository trainRepository;
    private final ScheduleRepository scheduleRepository;
    private final QuotaRepository quotaRepository;
    private final QuotaSeatAllocationRepository quotaSeatAllocationRepository;
    private final SeatAllocationService seatAllocationService;
    private final QuotaEligibilityValidator quotaEligibilityValidator;
    private final WaitlistRepository waitlistRepository;
    private final WaitlistService waitlistService;
    private final RacService racService;
    private final RacRepository racRepository;
    private final QuotaReservationPoolRepository quotaReservationPoolRepository;
    private final SeatHoldService seatHoldService;

    // private final BookingService bookingService;

    @Transactional
    public BookingResponse bookTicket(
            Long userId, BookingRequest bookingRequest, String idempotencyKey
    ) {
        Optional<Booking> exsistingBooking = bookingRepository.findByIdempotencyKey(idempotencyKey);
        if(exsistingBooking.isPresent()){
            Booking  booking = exsistingBooking.get();
            List<PassengerResponse> passengerResponses = booking.getPassengers().stream()
                    .map(passenger -> new PassengerResponse(
                            passenger.getName(),
                            passenger.getSeat() != null ? passenger.getSeat().getCoach().getCoachNumber() : null,
                            passenger.getSeat() != null ? passenger.getSeat().getSeatNumber() : null,
                            passenger.getSeat() != null ? passenger.getSeat().getBerthType() : null,
                            passenger.getPassengerStatus()
                    ))
                    .toList();
            return new BookingResponse(
                    booking.getPnr(),
                    booking.getBookingStatus(),
                    booking.getTotalFare(),
                    passengerResponses
            );
        }
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        //Find train
        Train train = trainRepository.findById(bookingRequest.trainId())
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + bookingRequest.trainId()));

        //Find Schedule
        Schedule schedule = scheduleRepository.findByTrainAndJourneyDate(train, bookingRequest.journeyDate())
                .orElseThrow(() -> new RuntimeException("Schedule not found for train: " + train.getId() + " on date: " + bookingRequest.journeyDate()));


        //Find Quota
        Quota quota = quotaRepository.findByCode(bookingRequest.quotaCode())
                .orElseThrow(() -> new RuntimeException("Quota not found with code: " + bookingRequest.quotaCode()));

        //so now you have all the details --> lets start creating a booking obj

        //Booking object
        Booking booking = Booking.builder()
                .pnr(generatePnr())
                .user(user)
                .schedule(schedule)
                .quota(quota)
                .idempotencyKey(idempotencyKey)
                .bookingStatus(BookingStatus.PENDING_PAYMENT)
                .totalFare(BigDecimal.ZERO) //
                .build();
        //Save booking
        Booking savedBooking = bookingRepository.save(booking);
        //here only booking object is created and it is not saved yet
        // still for a booking we need to create the passengers and allocate them the
        //seats

        //Part 2 (Passenger Loop + Seat Allocation + Quota Update)
        //Process Every Passenegr
        List<Passenger> passengers = new ArrayList<>();

        Set<Long> reservedSeatIds = new HashSet<>();
       List<QuotaSeatAllocation> updatedAllocations = new ArrayList<>();
        List<Waitlist> waitlists = new ArrayList<>();
        List<Rac> racs = new ArrayList<>();
        List<QuotaReservationPool> updatedReservationPools = new ArrayList<>();


        for (PassengerRequest passengerRequest : bookingRequest.passengers()) {
            quotaEligibilityValidator.validate(
                    passengerRequest,
                    user,
                    quota
            );
            //So firstly create the passenger --> since no seat has been allocated yet hence no passenger status is there
            Passenger singlePassenger = Passenger.builder()
                    .booking(booking)
                    .name(passengerRequest.name())
                    .age(passengerRequest.age())
                    .gender(passengerRequest.gender())
                    .berthPreference(passengerRequest.berthPreference())
                    .build();

            //Allocate Seat for this passenger -->  wrap it in the try -catch block so if the seat is not allocated it goes to the waiting list criteria
            try {
                System.out.println("========== BEFORE SEAT ALLOCATION ==========");
                Seat allocatedSeat = seatAllocationService.allocateSeat(
                        schedule,
                        bookingRequest.coachType(),
                        bookingRequest.quotaCode(),
                        reservedSeatIds
                );

                System.out.println("========== AFTER SEAT ALLOCATION ==========");
                System.out.println("Allocated Seat = " +
                        (allocatedSeat != null ? allocatedSeat.getId() : "NULL"));
                reservedSeatIds.add(allocatedSeat.getId());

                System.out.println("========== BEFORE SEAT HOLD ==========");
                System.out.println("Schedule ID: " + schedule.getId());
                System.out.println("Seat ID: " + allocatedSeat.getId());
                System.out.println("Booking ID: " + savedBooking.getId());
                //adding the redis hold after seat allocation

                boolean  seatHeld = seatHoldService.holdSeat(
                        schedule.getId(),
                        allocatedSeat.getId(),
                        savedBooking.getId()
                );
                System.out.println("=================================");
                System.out.println("SEAT HOLD EXECUTED");
                System.out.println("Schedule ID = " + schedule.getId());
                System.out.println("Seat ID     = " + allocatedSeat.getId());
                System.out.println("Booking ID  = " + savedBooking.getId());
                System.out.println("Seat Held   = " + seatHeld);
                System.out.println("=================================");
                if(!seatHeld) {
                    throw new RuntimeException("Failed to hold seat since the seat was held by another booking : " + allocatedSeat.getId());
                }

                //Find the quota allocation for the allocated coach

                QuotaSeatAllocation quotaSeatAllocation = quotaSeatAllocationRepository
                        .findByScheduleAndCoachAndQuota(
                                schedule,
                                allocatedSeat.getCoach(),
                                quota
                        )
                        .orElseThrow(() -> new RuntimeException("Quota Seat Allocation not found for schedule: " + schedule.getId() + ", coach: " + allocatedSeat.getCoach().getId() + ", quota: " + quota.getCode()));

                //reduce the available seats
                quotaSeatAllocation.setAvailableSeats(quotaSeatAllocation.getAvailableSeats() - 1);

                updatedAllocations.add(quotaSeatAllocation);
                //now the update the seat data for the passenger
                singlePassenger.setSeat(allocatedSeat);
                singlePassenger.setPassengerStatus(PassengerStatus.CONFIRMED);

            } catch (RuntimeException ex) {
                handleSeatUnavailable(
                        singlePassenger,
                        booking,
                        schedule,
                        quota,
                        updatedReservationPools,
                        racs,
                        waitlists
                );

            }
            passengers.add(singlePassenger);
        }
        //-------------Part 3 -----------
        //Calculate the fare
        BigDecimal totalFare = BigDecimal.valueOf(

                bookingRequest.passengers().size() * 500
        );
        booking.setTotalFare(totalFare);


        //save Passenger
        passengerRepository.saveAll(passengers);

        racRepository.saveAll(racs);

        //SAVE THE WAITLIST LIST ALSO
        waitlistRepository.saveAll(waitlists);
        //save updated QuotaAllocation
        quotaSeatAllocationRepository.saveAll(updatedAllocations);
        quotaReservationPoolRepository.saveAll(updatedReservationPools);

        //Build the passenger response
        List<PassengerResponse> passengerResponses = passengers.stream()
                .map(passenger -> new PassengerResponse(
                                passenger.getName(),
                                passenger.getSeat() != null ? passenger.getSeat().getCoach().getCoachNumber() : null,
                                passenger.getSeat() != null ? passenger.getSeat().getSeatNumber() : null,
                                passenger.getSeat() != null ? passenger.getSeat().getBerthType() : null,
                                passenger.getPassengerStatus()
                        )

                )
                .toList();
        return new BookingResponse(

                savedBooking.getPnr(),

                savedBooking.getBookingStatus(),

                savedBooking.getTotalFare(),

                passengerResponses);

    }

    public BookingResponse getBookingByPnr(String pnr) {

        //firstly we need to find the pnr
        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new RuntimeException("Booking not found with PNR: " + pnr));

        //Convert the passenger entity in to passenger resposne
        List<PassengerResponse> passengerResponses = booking.getPassengers().stream()
                .map(passenger -> new PassengerResponse(
                        passenger.getName(),
                        passenger.getSeat().getCoach().getCoachNumber(),
                        passenger.getSeat().getSeatNumber(),
                        passenger.getSeat().getBerthType(),
                        passenger.getPassengerStatus()
                ))
                .toList();
        return new BookingResponse(
                booking.getPnr(),
                booking.getBookingStatus(),
                booking.getTotalFare(),
                passengerResponses
        );
    }

    //helper function
    /*
    what is the use of this method
    -> check the rac availability
    -> allocate rac if possible
    -> otherwise create waitlist entry
    -> update passenger status
    -> update booking status

     */
    private void handleSeatUnavailable(
            Passenger passenger,
            Booking booking,
            Schedule schedule,
            Quota quota,
           // Coach coach,
            //List for storing
           // List<QuotaSeatAllocation> updatedAllocations,
            List<QuotaReservationPool> updatedReservationPools,
            List<Rac> racEntries,
            List<Waitlist> waitlists

    ) {
        //Finding the quota seat allocation
        QuotaReservationPool reservationPool =
                quotaReservationPoolRepository
                        .findByScheduleAndQuota(schedule, quota)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Quota Reservation Pool not found."
                                )
                        );
        //-----------Rac----------------

        if (reservationPool.getRacAvailable() > 0) {
            //decrease the rac available count
            reservationPool.setRacAvailable(reservationPool.getRacAvailable() - 1);
            updatedReservationPools.add(reservationPool);
            //add to the list
            //updatedAllocations.add(reservationPool);

            Rac rac = racService.createRacEntry(schedule,quota,  passenger);
            racEntries.add(rac);
            passenger.setPassengerStatus(PassengerStatus.RAC);
            booking.setBookingStatus(BookingStatus.PENDING_PAYMENT);

            return;
        }
        //----------- if the rac is not available then set it to waitlist ----------

        if (reservationPool.getWaitlistAvailable() > 0) {
            //decrease the waitlist available count
            reservationPool.setWaitlistAvailable(reservationPool.getWaitlistAvailable() - 1);
            updatedReservationPools.add(reservationPool);

            Waitlist waitlist = waitlistService.createWaitlistEntry(schedule, quota , passenger);
            waitlists.add(waitlist);
            passenger.setPassengerStatus(PassengerStatus.WAITLISTED);
            booking.setBookingStatus(BookingStatus.WAITLIST);

            return;
        }

        //-----------It is full--------------
        throw new RuntimeException("No confirmed ,RAC or waitlist seats available for passenger: " + passenger.getName());
    }


    //PNR Generator
    private String generatePnr() {

        return UUID.randomUUID().toString()
                .replace("-", "").substring(0, 10)
                .toUpperCase();
    }


}

