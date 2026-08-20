package com.soham.railway_reservation_engine.common.enums;

/**
 * Lifecycle of a train's journey on a specific date.
 *
 * <p>OPEN → CHART_PREPARING (atomic CAS in {@code ScheduleRepository.markChartPreparing},
 * only one job may win) → CHART_PREPARED. Once charted, no further bookings are accepted;
 * pending waitlisted passengers are promoted to RAC/CONFIRMED as capacity allows.
 */
public enum ScheduleStatus {
    OPEN,
    CLOSED,
    CHART_PREPARING,
    CHART_PREPARED
}
