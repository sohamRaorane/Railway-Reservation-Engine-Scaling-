
package com.soham.railway_reservation_engine.payment.service;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.common.enums.BookingStatus;
import com.soham.railway_reservation_engine.common.enums.PassengerStatus;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.quotaReservationPool.entity.QuotaReservationPool;
import com.soham.railway_reservation_engine.quotaReservationPool.repository.QuotaReservationPoolRepository;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.quotaSeatAllocation.repository.QuotaSeatAllocationRepository;
import com.soham.railway_reservation_engine.rac.entity.Rac;
import com.soham.railway_reservation_engine.rac.repository.RacRepository;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.seat.entity.Seat;
import com.soham.railway_reservation_engine.seat.service.SeatHoldService;
import com.soham.railway_reservation_engine.waitlist.entity.Waitlist;
import com.soham.railway_reservation_engine.waitlist.repository.WaitlistRepository;
import com.soham.railway_reservation_engine.waitlist.service.WaitlistPromotionService;
import com.soham.railway_reservation_engine.waitlist.service.WaitlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentFailureService {

    private final QuotaSeatAllocationRepository quotaSeatAllocationRepository;
    private final QuotaReservationPoolRepository quotaReservationPoolRepository;
    private final RacRepository racRepository;
    private final WaitlistRepository waitlistRepository;
    private final WaitlistPromotionService waitlistPromotionService;
    private final SeatHoldService seatHoldService;


    public void handlePaymentFailure(Booking booking) {
        for(Passenger passenger : booking.getPassengers()) {
            if(passenger.getSeat() != null) {
                seatHoldService.releaseSeat(
                        booking.getSchedule().getId(),
                        passenger.getSeat().getId()
                );
            }
        }
    //Fetching the schedule
        Schedule schedule = booking.getSchedule();

        QuotaReservationPool pool =
                quotaReservationPoolRepository
                        .findByScheduleAndQuota(
                                schedule,
                                booking.getQuota()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Quota reservation pool not found."
                                )
                        );
        int racSlotsFreed = 0;
        int confirmedSeatsFreed = 0;
        for (Passenger passenger : booking.getPassengers()) {

            PassengerStatus status =
                    passenger.getPassengerStatus();

            switch (status) {
                //--------Confirmed case--------
                case CONFIRMED -> {
                    if (passenger.getSeat() == null) {

                        throw new IllegalStateException(
                                "Confirmed passenger has no seat. Passenger ID: "
                                        + passenger.getId()
                        );
                    }
                    QuotaSeatAllocation allocation =
                            quotaSeatAllocationRepository
                                    .findByScheduleAndCoachAndQuota(schedule,
                                            passenger.getSeat().getCoach(),
                                            booking.getQuota()
                                    )
                                    .orElseThrow(() ->
                                            new RuntimeException(
                                                    "Quota seat allocation not found."
                                            )
                                    );
                    allocation.setAvailableSeats(
                            allocation.getAvailableSeats() + 1
                    );

                    quotaSeatAllocationRepository.save(allocation);

                    passenger.setSeat(null);

                    passenger.setPassengerStatus(
                            PassengerStatus.CANCELLED
                    );
                    confirmedSeatsFreed++;
                }
                //------------ RAC Case ------------
                case RAC -> {
                    Rac rac =
                            racRepository
                                    .findByPassenger(passenger)
                                    .orElseThrow(() ->
                                            new RuntimeException(
                                                    "RAC entry not found for passenger: "
                                                            + passenger.getId()
                                            )
                                    );

                    racRepository.delete(rac);

                    pool.setRacAvailable(
                            pool.getRacAvailable() + 1
                    );
                    racSlotsFreed++;
                    passenger.setPassengerStatus(
                            PassengerStatus.CANCELLED
                    );
                }


                case WAITLISTED -> {

                    Waitlist waitlist =
                            waitlistRepository
                                    .findByPassenger(passenger)
                                    .orElseThrow(() ->
                                            new RuntimeException(
                                                    "Waitlist entry not found for passenger: "
                                                            + passenger.getId()
                                            )
                                    );


                    waitlistRepository.delete(waitlist);

                    pool.setWaitlistAvailable(
                            pool.getWaitlistAvailable() + 1
                    );

                    passenger.setPassengerStatus(
                            PassengerStatus.CANCELLED
                    );
                }
                default -> {

                    throw new IllegalStateException(
                            "Cannot process payment failure for passenger "
                                    + passenger.getId()
                                    + " because passenger status is "
                                    + status
                    );
                }
            }
        }

        quotaReservationPoolRepository.save(pool);
        //failed rac booking can free one or more seats
        for(int i = 0 ; i < racSlotsFreed ; i++) {
            waitlistPromotionService.promoteWaitlistToRac(schedule, booking.getQuota());
        }
        for(int i = 0 ; i < confirmedSeatsFreed ; i++) {
            waitlistPromotionService.promotePassenger(schedule, booking.getQuota());
        }
        booking.setBookingStatus(
                BookingStatus.CANCELLED
        );

    }
}

