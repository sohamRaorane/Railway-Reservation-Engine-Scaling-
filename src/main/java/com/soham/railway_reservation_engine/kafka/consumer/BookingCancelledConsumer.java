package com.soham.railway_reservation_engine.kafka.consumer;

import com.soham.railway_reservation_engine.bookings.event.BookingCancelledEvent;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quota.repository.QuotaRepository;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.schedule.repository.ScheduleRepository;
import com.soham.railway_reservation_engine.waitlist.service.WaitlistPromotionService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingCancelledConsumer {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(BookingCancelledConsumer.class);

    private final ScheduleRepository scheduleRepository;
    private final QuotaRepository quotaRepository;
    private final WaitlistPromotionService waitlistPromotionService;

    @KafkaListener(
            topics = "booking.cancelled",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeBookingCancelled(BookingCancelledEvent event) {
        log.info(
                "booking.cancelled received: bookingId={}, pnr={}, scheduleId={}, quotaId={}",
                event.bookingId(),
                event.pnr(),
                event.scheduleId(),
                event.quotaId()
        );

        // Find schedule
        Schedule schedule = scheduleRepository.findById(event.scheduleId()).orElse(null);
        if (schedule == null) {
            log.error("booking.cancelled dropped: schedule not found with id={}", event.scheduleId());
            return;
        }

        // Find quota
        Quota quota = quotaRepository.findById(event.quotaId()).orElse(null);
        if (quota == null) {
            log.error("booking.cancelled dropped: quota not found with id={}", event.quotaId());
            return;
        }

        log.info("booking.cancelled promotion starting: scheduleId={}, quotaId={}", schedule.getId(), quota.getId());

        waitlistPromotionService.promotePassenger(
                schedule,
                quota
        );

        log.info("booking.cancelled promotion completed: bookingId={}, pnr={}", event.bookingId(), event.pnr());
    }
}
