package com.soham.railway_reservation_engine.kafka.consumer;

import com.soham.railway_reservation_engine.bookings.event.BookingCancelledEvent;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quota.repository.QuotaRepository;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.schedule.repository.ScheduleRepository;
import com.soham.railway_reservation_engine.waitlist.service.WaitlistPromotionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Business-effect consumer of {@code booking.cancelled}: triggers waitlist/RAC promotion.
 *
 * <p><b>Flow:</b> on a cancellation event it reloads the schedule and quota by id, then delegates
 * to {@code WaitlistPromotionService.promotePassenger} to upgrade the next waiting passenger into
 * the freed slot. These steps run <i>asynchronously</i> from the HTTP cancellation request thanks
 * to Kafka.
 *
 * <p><b>Advanced logging concept:</b> the correlation id (carried inside the event by the
 * producer) is restored into the MDC before processing and removed in a {@code finally} block.
 * Kafka consumer threads are reused across messages, so leaving the MDC set would leak the id
 * into unrelated log lines of later messages.
 */
@Component
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

        try {
            // Restore correlation ID from the Kafka event
            MDC.put("correlationId", event.correlationId());

            log.info(
                    "booking.cancelled received: bookingId={}, pnr={}, scheduleId={}, quotaId={}",
                    event.bookingId(),
                    event.pnr(),
                    event.scheduleId(),
                    event.quotaId()
            );

            // Find schedule
            Schedule schedule =
                    scheduleRepository.findById(event.scheduleId()).orElse(null);

            if (schedule == null) {
                log.error(
                        "booking.cancelled dropped: schedule not found with id={}",
                        event.scheduleId()
                );
                return;
            }

            // Find quota
            Quota quota =
                    quotaRepository.findById(event.quotaId()).orElse(null);

            if (quota == null) {
                log.error(
                        "booking.cancelled dropped: quota not found with id={}",
                        event.quotaId()
                );
                return;
            }

            log.info(
                    "booking.cancelled promotion starting: scheduleId={}, quotaId={}, freedSeatCount={}",
                    schedule.getId(),
                    quota.getId(),
                    event.freedSeatCount()
            );

            // Run one promotion per released confirmed seat: a cancelled booking with N confirmed
            // passengers frees N seats, so N waiting passengers can be moved up. promotePassenger
            // locks the quota pool per call and no-ops cleanly when nothing is waiting, so repeated
            // calls are safe.
            for (int i = 0; i < event.freedSeatCount(); i++) {
                waitlistPromotionService.promotePassenger(
                        schedule,
                        quota
                );
            }

            log.info(
                    "booking.cancelled promotion completed: bookingId={}, pnr={}",
                    event.bookingId(),
                    event.pnr()
            );

        } finally {
            // Very important for Kafka consumer threads
            MDC.remove("correlationId");
        }
    }
}