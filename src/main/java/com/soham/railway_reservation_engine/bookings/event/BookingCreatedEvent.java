package com.soham.railway_reservation_engine.bookings.event;

/**
 * Kafka message payload for the {@code booking.created} topic.
 *
 * <p>Carries only what downstream consumers need (booking id, PNR, schedule/quota ids, and the
 * correlation id for log tracing). Records are ideal event payloads: immutable and automatically
 * equipped with accessors, {@code equals}/{@code hashCode} and a canonical constructor. Published
 * after the booking transaction commits; the notification consumer reacts to it.
 */
public record BookingCreatedEvent(

        Long bookingId,
        String pnr,
        Long scheduleId,
        Long quotaId,
        String correlationId
) {
}

