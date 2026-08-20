package com.soham.railway_reservation_engine.bookings.service;

import com.soham.railway_reservation_engine.bookings.strategy.SeatAllocationStrategy;
import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.common.enums.CoachType;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.seat.entity.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor

/**
 * Entry point for seat allocation.
 *
 * <p>Uses the <b>Strategy design pattern</b>: the actual selection algorithm is pluggable.
 * Today the only implementation is {@link FirstAvailableSeatStrategy}, but swapping in an
 * algorithm that honours berth preferences or distributes seats across coaches later requires
 * no change to callers — they just depend on the {@link SeatAllocationStrategy} interface.
 * This is the "context" class of the pattern.
 */
public class SeatAllocationService {
    private final SeatAllocationStrategy seatAllocationStrategy;

    public Seat allocateSeat(
            Schedule schedule,
            CoachType coachType,
            String quotaCode,
            Set<Long> reservedSeatIds
    ){
        return  seatAllocationStrategy.allocate(
                schedule,
                coachType,
                quotaCode,
                reservedSeatIds
        );
    }
}
