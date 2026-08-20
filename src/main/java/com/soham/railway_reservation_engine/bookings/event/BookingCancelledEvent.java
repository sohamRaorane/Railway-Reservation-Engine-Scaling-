package com.soham.railway_reservation_engine.bookings.event;

/**
 * Kafka message payload for the {@code booking.cancelled} topic.
 *
 * <p>Consumed by two independent consumers: {@code BookingCancelledConsumer} (triggers waitlist/RAC
 * promotion) and {@code BookingNotificationConsumer} (emits the cancellation notification). The
 * correlation id lets logs be traced across producer and consumer processes.
 *
 * <p>{@code freedSeatCount} is the number of confirmed seats released by the cancellation — one
 * promotion must run per freed seat (e.g. cancelling a 3-passenger confirmed booking frees 3 seats
 * and needs 3 promotions), otherwise waiting passengers stall until some unrelated event triggers
 * them.
 */
public record BookingCancelledEvent(
        Long bookingId,
        String pnr,
        Long scheduleId,
        Long quotaId,
        int freedSeatCount,
        String correlationId

) {

}
