package com.soham.railway_reservation_engine.kafka.consumer;


import com.soham.railway_reservation_engine.bookings.event.BookingCancelledEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Notification consumer of {@code booking.cancelled}.
 *
 * <p><b>Multiple consumers, one topic:</b> this consumer runs in its own consumer group
 * ({@code railway-notification-group}), separate from {@code BookingCancelledConsumer}. Kafka
 * delivers every event to each group, so the same message independently triggers both promotion
 * and notification — this is the publish/subscribe model. A separate group also means the
 * notification pipeline can be scaled or deployed independently without disturbing promotion.
 */
@Component
@RequiredArgsConstructor
public class BookingNotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(BookingNotificationConsumer.class);

    @KafkaListener(
            topics = "booking.cancelled",
            groupId = "railway-notification-group"
    )
    public void consumeBookingCancelled(BookingCancelledEvent event) {

        log.info(
                "NOTIFICATION SENT | correlationId={} | bookingId={} | pnr={} | message=Booking cancelled successfully",
                event.correlationId(),
                event.bookingId(),
                event.pnr()
        );
    }

    }

