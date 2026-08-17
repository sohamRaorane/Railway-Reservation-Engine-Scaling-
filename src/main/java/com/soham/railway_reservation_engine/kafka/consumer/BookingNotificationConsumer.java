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

        System.out.println("==========================================");
        System.out.println("      NOTIFICATION CONSUMER TRIGGERED");
        System.out.println("==========================================");
        System.out.println("Booking ID     : " + event.bookingId());
        System.out.println("PNR            : " + event.pnr());
        System.out.println("Correlation ID : " + event.correlationId());
        System.out.println("Message        : Booking cancelled successfully");
        System.out.println("==========================================");

        log.info(
                "NOTIFICATION SENT | correlationId={} | bookingId={} | pnr={} | message=Booking cancelled successfully",
                event.correlationId(),
                event.bookingId(),
                event.pnr()
        );
    }

    }

