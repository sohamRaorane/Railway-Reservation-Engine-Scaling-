package com.soham.railway_reservation_engine.kafka.producer;


import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.bookings.event.BookingCancelledEvent;
import com.soham.railway_reservation_engine.bookings.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingEventProducer {
    private static final String BOOKING_CREATED_TOPIC = "booking.created";
    private static final String BOOKING_CANCELLED_TOPIC = "booking.cancelled";

    //Spring abstraction for sending the messages
    private final KafkaTemplate<String, Object > kafkaTemplate;
    public void publishBookingCreated(BookingCreatedEvent bookingCreatedEvent) {
        //by using the pnr as the key it gives us a stable identity for the booking event
        kafkaTemplate.send(BOOKING_CREATED_TOPIC,bookingCreatedEvent.pnr(), bookingCreatedEvent);
    }

    public void publishBookingCancelled(BookingCancelledEvent bookingCancelledEvent) {
        kafkaTemplate.send(BOOKING_CANCELLED_TOPIC,bookingCancelledEvent.pnr(), bookingCancelledEvent);
    }


}
