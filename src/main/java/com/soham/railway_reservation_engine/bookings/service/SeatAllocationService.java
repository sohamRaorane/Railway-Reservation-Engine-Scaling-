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

//it becomes the entry point for the seat allocation
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
