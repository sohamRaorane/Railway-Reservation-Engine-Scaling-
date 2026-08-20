package com.soham.railway_reservation_engine.kafka.consumer;


import com.soham.railway_reservation_engine.bookings.event.BookingCancelledEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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

