package com.soham.railway_reservation_engine.waitlist.service;


import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.bookings.repository.BookingRepository;
import com.soham.railway_reservation_engine.bookings.service.SeatAllocationService;
import com.soham.railway_reservation_engine.common.enums.BookingStatus;
import com.soham.railway_reservation_engine.common.enums.CoachType;
import com.soham.railway_reservation_engine.common.enums.PassengerStatus;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.passenger.repository.PassengerRepository;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quota.repository.QuotaRepository;
import com.soham.railway_reservation_engine.quotaReservationPool.entity.QuotaReservationPool;
import com.soham.railway_reservation_engine.quotaReservationPool.repository.QuotaReservationPoolRepository;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.quotaSeatAllocation.repository.QuotaSeatAllocationRepository;
import com.soham.railway_reservation_engine.rac.entity.Rac;
import com.soham.railway_reservation_engine.rac.repository.RacRepository;
import com.soham.railway_reservation_engine.rac.service.RacService;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.seat.entity.Seat;
import com.soham.railway_reservation_engine.waitlist.entity.Waitlist;
import com.soham.railway_reservation_engine.waitlist.repository.WaitlistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Moves waiting passengers up the reservation ladder whenever capacity frees up.
 *
 * <p><b>The promotion ladder (per schedule+quota):</b>
 * <ol>
 *   <li>{@code promoteWaitlistToRac} — top waitlist entry → RAC, when an RAC slot frees.</li>
 *   <li>{@code promoteWaitlistToConfirmed} — top waitlist entry → confirmed seat, when a
 *       confirmed seat frees (used at chart time).</li>
 *   <li>{@code promotePassenger} — RAC → confirmed seat, then back-fills the vacated RAC slot
 *       from the waitlist (RAC→CONFIRMED first, then WL→RAC) — the full one-slot cascade.</li>
 * </ol>
 *
 * <p>Every method first locks the {@code QuotaReservationPool} row ({@code findForUpdate}) so
 * promotion logic for the same quota is fully serialised — otherwise two concurrent cancellations
 * could promote the same waitlist entry twice. Seat allocation reuses the exact same strategy the
 * booking flow uses, so the same concurrency protections apply.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WaitlistPromotionService {

    private final RacRepository racRepository;
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final QuotaReservationPoolRepository quotaReservationPoolRepository;
    private final WaitlistRepository waitlistRepository;
    private final RacService racService;
    private final SeatAllocationService seatAllocationService;
    private final QuotaSeatAllocationRepository quotaSeatAllocationRepository;

    public void promotePassenger(
            Schedule schedule,
            Quota quota
    ){
        //lock the quota  reservation pool for this schedule + quota
        //this serialize RAC/WL promotion for the same quota
        QuotaReservationPool reservationPool = quotaReservationPoolRepository.findForUpdate(schedule, quota)
                .orElseThrow(() -> new RuntimeException("Quota reservation pool not found for the given schedule and quota"));

        Rac rac = racRepository.findFirstByScheduleAndQuotaOrderByRacNumberAsc(schedule, quota)
                .orElse(null);
        if (rac == null) {

            promoteWaitlistToConfirmed(
                    schedule,
                    quota
            );

            return;
        }

        Passenger passenger = rac.getPassenger();
        Booking booking = passenger.getBooking();
        CoachType coachType = booking.getCoachType();

        if (coachType == null) {
            throw new IllegalStateException(
                    "Coach type is not set for booking: "
                            + booking.getPnr()
            );
        }
        Seat allocatedSeat = seatAllocationService.allocateSeat(
                schedule,
                coachType,
                quota.getCode(),
                new HashSet<>()
        );
        passenger.setSeat(allocatedSeat);
        passenger.setPassengerStatus(
                PassengerStatus.CONFIRMED
        );
        booking.setBookingStatus(
                BookingStatus.CONFIRMED
        );
        QuotaSeatAllocation quotaSeatAllocation =
                quotaSeatAllocationRepository
                        .findByScheduleAndCoachAndQuota(
                                schedule,
                                allocatedSeat.getCoach(),
                                quota
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Quota seat allocation not found."
                                )
                        );

        quotaSeatAllocation.setAvailableSeats(
                quotaSeatAllocation.getAvailableSeats() - 1
        );

        quotaSeatAllocationRepository.save(quotaSeatAllocation);



        reservationPool.setRacAvailable(reservationPool.getRacAvailable() + 1);
        racRepository.delete(rac);

        //check if anyone is waiting
        Waitlist  waitlist = waitlistRepository.findFirstByScheduleAndQuotaOrderByWaitlistNumberAsc(
                schedule, quota
        ).orElse(null);

        if(waitlist == null) return;
        //first waitlisted passeneger
        Passenger waitlistedPassenger = waitlist.getPassenger();
        //promote wl --> rac
        waitlistedPassenger.setPassengerStatus(PassengerStatus.RAC);

        //create a new rac entry
        Rac newRac = racService.createRacEntry(schedule, quota, waitlistedPassenger);
        racRepository.save(newRac);

        //remove from the waitlist
        waitlistRepository.delete(waitlist);

        //RAC slot occuiped again
        reservationPool.setRacAvailable(reservationPool.getRacAvailable() - 1);

        //one waitlist becomes free
        reservationPool.setWaitlistAvailable(reservationPool.getWaitlistAvailable() + 1);

    }

    public  void promoteWaitlistToRac(Schedule schedule, Quota quota){
        //lock  quota pool before generating rac number
        QuotaReservationPool reservationPool = quotaReservationPoolRepository.findForUpdate(schedule, quota)
                .orElseThrow(() -> new RuntimeException("Quota reservation pool not found for the schedule and quota"));

        Waitlist waitlist = waitlistRepository.findFirstByScheduleAndQuotaOrderByWaitlistNumberAsc(schedule, quota)
                .orElse(null);
        if(waitlist == null) return;
        Passenger passenger =  waitlist.getPassenger();
        passenger.setPassengerStatus(PassengerStatus.RAC);
        Rac rac = racService.createRacEntry(schedule, quota, passenger);
        racRepository.save(rac);

        waitlistRepository.delete(waitlist);

        reservationPool.setRacAvailable(reservationPool.getRacAvailable() - 1);

        // so now the waitlist position has became available hence
        reservationPool.setWaitlistAvailable(reservationPool.getWaitlistAvailable() + 1);

        //at the end of trancstion commit hibernate persists the modified pool
    }


    public  void promoteWaitlistToConfirmed(
            Schedule schedule,
            Quota quota
    ) {
        QuotaReservationPool reservationPool = quotaReservationPoolRepository.findForUpdate(schedule, quota)
                .orElseThrow(() -> new RuntimeException("Quota reservation pool not found for the given schedule and quota"));

        Waitlist waitlist = waitlistRepository
                .findFirstByScheduleAndQuotaOrderByWaitlistNumberAsc(
                        schedule,
                        quota
                )
                .orElse(null);

        if (waitlist == null) {
            return;
        }

        Passenger passenger = waitlist.getPassenger();
        Booking booking = passenger.getBooking();
        CoachType coachType = booking.getCoachType();

        if(coachType == null){
            throw new RuntimeException("Coach type is not set for the booking." + booking.getPnr());
        }
        //Allocate a physical seat here
        Set<Long> reservedSeatIds = new HashSet<>();
        Seat allocatedSeat = seatAllocationService.allocateSeat(
                schedule,
                coachType,
                quota.getCode(),
                reservedSeatIds
        );
        passenger.setSeat(allocatedSeat);

        passenger.setPassengerStatus(
                PassengerStatus.CONFIRMED
        );

      //  Booking booking = passenger.getBooking();

        booking.setBookingStatus(
                BookingStatus.CONFIRMED
        );

        waitlistRepository.delete(waitlist);



        reservationPool.setWaitlistAvailable(
                reservationPool.getWaitlistAvailable() + 1
        );

        QuotaSeatAllocation quotaSeatAllocation = quotaSeatAllocationRepository
                .findByScheduleAndCoachAndQuota(schedule, allocatedSeat.getCoach(), quota)
                .orElseThrow(() -> new RuntimeException("Quota seat allocation not found."));

        //now we have moved the passeneger to the confirmeed seat so hence decrement it
        quotaSeatAllocation.setAvailableSeats(
                quotaSeatAllocation.getAvailableSeats() - 1
        );
        quotaSeatAllocationRepository.save(quotaSeatAllocation);
        quotaReservationPoolRepository.save(reservationPool);

    }




}
