package com.soham.railway_reservation_engine.kafka.producer;

import com.soham.railway_reservation_engine.bookings.event.BookingCancelledEvent;
import com.soham.railway_reservation_engine.bookings.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publish side of the event pipeline — sends booking lifecycle events to Kafka.
 *
 * <p><b>Why Kafka instead of calling the consumers directly?</b> The producer fires events
 * <i>after</i> the booking transaction commits. Downstream work (waitlist promotion, sending
 * notifications) is decoupled, retried by the broker, and can scale independently. If it were
 * in-line, a slow notification could block the booking request itself.
 *
 * <p><b>Partitioning:</b> the PNR is used as the message key, so all events for the same booking
 * land in the same partition and are processed in order (per-partition ordering guarantee).
 */
@Component
@RequiredArgsConstructor
public class BookingEventProducer {
    private static final String BOOKING_CREATED_TOPIC = "booking.created";
    private static final String BOOKING_CANCELLED_TOPIC = "booking.cancelled";

    //Spring abstraction for sending the messages
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBookingCreated(BookingCreatedEvent bookingCreatedEvent) {
        // By using the PNR as the key, we get a stable identity for the booking event
        kafkaTemplate.send(BOOKING_CREATED_TOPIC, bookingCreatedEvent.pnr(), bookingCreatedEvent);
    }

    public void publishBookingCancelled(BookingCancelledEvent bookingCancelledEvent) {
        kafkaTemplate.send(BOOKING_CANCELLED_TOPIC, bookingCancelledEvent.pnr(), bookingCancelledEvent);
    }


}
