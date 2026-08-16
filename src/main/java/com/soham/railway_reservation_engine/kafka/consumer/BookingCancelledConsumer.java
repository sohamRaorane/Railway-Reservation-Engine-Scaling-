package com.soham.railway_reservation_engine.kafka.consumer;

import com.soham.railway_reservation_engine.bookings.event.BookingCancelledEvent;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.quota.repository.QuotaRepository;
import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.schedule.repository.ScheduleRepository;
import com.soham.railway_reservation_engine.waitlist.service.WaitlistPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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

        System.out.println("\n");
        System.out.println("================================================");
        System.out.println("BOOKING CANCELLED KAFKA EVENT RECEIVED ");
        System.out.println("================================================");
        System.out.println("Booking ID  : " + event.bookingId());
        System.out.println("PNR         : " + event.pnr());
        System.out.println("Schedule ID : " + event.scheduleId());
        System.out.println("Quota ID    : " + event.quotaId());
        System.out.println("Consumer    : BookingCancelledConsumer");
        System.out.println("================================================");
        System.out.println("\n");

        // Find schedule
        Schedule schedule = scheduleRepository.findById(event.scheduleId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Schedule not found with id: "
                                        + event.scheduleId()
                        )
                );

        // Find quota
        Quota quota = quotaRepository.findById(event.quotaId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Quota not found with id: "
                                        + event.quotaId()
                        )
                );

        System.out.println("========== PROMOTION STARTING ==========");
        System.out.println("Schedule ID = " + schedule.getId());
        System.out.println("Quota ID    = " + quota.getId());

        waitlistPromotionService.promotePassenger(
                schedule,
                quota
        );

        System.out.println("========== PROMOTION COMPLETED ==========");
    }
}
