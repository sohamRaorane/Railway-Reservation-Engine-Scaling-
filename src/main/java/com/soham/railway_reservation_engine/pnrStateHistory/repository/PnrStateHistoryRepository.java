package com.soham.railway_reservation_engine.pnrStateHistory.repository;

import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.pnrStateHistory.entity.PnrStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PnrStateHistoryRepository
        extends JpaRepository<PnrStateHistory, Long> {

    List<PnrStateHistory> findByBookingOrderByChangedAtAsc(Booking booking);
}