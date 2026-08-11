package com.soham.railway_reservation_engine.bookings.strategy;

import com.soham.railway_reservation_engine.common.enums.CoachType;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.seat.entity.Seat;

import java.util.Set;

public interface SeatAllocationStrategy {

    Seat allocate(
            Schedule schedule,
            CoachType coachType,
            String quotaCode,
            Set<Long> reservedSeatIds
    );
}
