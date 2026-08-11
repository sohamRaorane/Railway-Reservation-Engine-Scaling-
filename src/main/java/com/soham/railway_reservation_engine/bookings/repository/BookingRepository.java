package com.soham.railway_reservation_engine.bookings.repository;


import com.soham.railway_reservation_engine.bookings.entity.Booking;


import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPnr(String pnr);
    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

}
