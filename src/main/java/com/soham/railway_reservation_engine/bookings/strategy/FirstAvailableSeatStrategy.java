package com.soham.railway_reservation_engine.bookings.strategy;

import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.coach.repository.CoachRepository;
import com.soham.railway_reservation_engine.common.enums.CoachType;
import com.soham.railway_reservation_engine.passenger.repository.PassengerRepository;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quota.repository.QuotaRepository;
import com.soham.railway_reservation_engine.quotaSeatAllocation.entity.QuotaSeatAllocation;
import com.soham.railway_reservation_engine.quotaSeatAllocation.repository.QuotaSeatAllocationRepository;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.seat.entity.Seat;
import com.soham.railway_reservation_engine.seat.repository.SeatRepository;
import com.soham.railway_reservation_engine.seat.service.SeatHoldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FirstAvailableSeatStrategy  implements SeatAllocationStrategy{

    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;
    private final PassengerRepository passengerRepository;
    private final QuotaSeatAllocationRepository quotaSeatAllocationRepository;
    private final QuotaRepository quotaRepository;
    private final SeatHoldService   seatHoldService;

    @Override
    public Seat allocate(
            Schedule schedule,
            CoachType coachType,
            String quotaCode,
            Set<Long> reservedSeatIds
    ) {

        // Step 1 : Find the quota entity
        //since i have setup the quota code as string we will need to convert it to  the
        //quota entity first because the quota allocation repository needs the quota as a input
        Quota quota = quotaRepository.findByCode(quotaCode)
                .orElseThrow(() ->
                        new RuntimeException("Quota not found : " + quotaCode));

        // Step 2 : Find all coaches of the requested coach type
        List<Coach> coaches = coachRepository.findByTrainAndCoachType(
                schedule.getTrain(),
                coachType
        );

        if (coaches.isEmpty()) {
            throw new RuntimeException(
                    "No coaches found for coach type : " + coachType
            );
        }

        // Step 3 : Fetch already booked seat ids
        Set<Long> bookedSeatIds = new HashSet<>(
                passengerRepository.findBookedSeatIdsByScheduleId(schedule)
        );
        //Add the seats  already allocated during this booking
        bookedSeatIds.addAll(reservedSeatIds);

        // Step 4 : Iterate through each coach
        for (Coach coach : coaches) {

            Optional<QuotaSeatAllocation> allocationOptional =
                    quotaSeatAllocationRepository
                            .findByScheduleAndCoachAndQuota(
                                    schedule,
                                    coach,
                                    quota
                            );

            if (allocationOptional.isEmpty()) {
                continue;
            }

            QuotaSeatAllocation allocation = allocationOptional.get();

            if (allocation.getAvailableSeats() <= 0) {
                continue;
            }

            List<Seat> seats =
                    seatRepository.findByCoachOrderBySeatNumberAsc(coach);

            for (Seat seat : seats) {
                /*
                Pessimistic locking ensures that when a transaction finds a candidate seat, it acquires a database row-level lock using PESSIMISTIC_WRITE (SELECT ... FOR UPDATE).
                 Other transactions trying to lock the same seat must wait until the first transaction commits or rolls back.
                  We then re-check the seat after acquiring the lock because its availability may have changed before the lock was obtained.
                  If it is booked or Redis-held, we skip it; otherwise, we return the locked seat for allocation.
                Redis and PostgreSQL locking work independently: Redis handles temporary payment-window holds, while PostgreSQL locking protects concurrent database transactions.
                 */
                if (bookedSeatIds.contains(seat.getId())) {
                    continue;
                }
                //Someone is holding this seat --> so do not give it to anybody else
                boolean redisHeld =
                        seatHoldService.isSeatHeld(schedule.getId(), seat.getId());
                if (redisHeld) {
                    continue;
                }

                // Acquire database row lock --> lock this row for the database transction
                Optional<Seat> lockedSeat =
                        seatRepository.findByIdForUpdate(seat.getId());
                if (lockedSeat.isEmpty()) {
                    continue;
                }
                Seat seatForUpdate = lockedSeat.get();
            //Standard concurrency pattern --> Check -> Lock -> Recheck -> Use
            // Re-check after acquiring the database lock
                if (bookedSeatIds.contains(seatForUpdate.getId())) {
                    continue;
                }

                boolean redisHeldAfterLock =
                        seatHoldService.isSeatHeld(
                                schedule.getId(),
                                seatForUpdate.getId()
                        );

                if (redisHeldAfterLock) {
                    continue;
                }

                return seatForUpdate;            }
        }

        throw new RuntimeException(
                "No seat available for coach type " +
                        coachType +
                        " under quota " +
                        quotaCode
        );
    }
}

