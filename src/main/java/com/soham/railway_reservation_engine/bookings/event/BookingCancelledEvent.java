package com.soham.railway_reservation_engine.bookings.event;

/**
 * Kafka message payload for the {@code booking.cancelled} topic.
 *
 * <p>Consumed by two independent consumers: {@code BookingCancelledConsumer} (triggers waitlist/RAC
 * promotion) and {@code BookingNotificationConsumer} (emits the cancellation notification). The
 * correlation id lets logs be traced across producer and consumer processes.
 */
public record BookingCancelledEvent(
        Long bookingId,
        String pnr,
        Long scheduleId,
        Long quotaId,
        String correlationId

) {

}
