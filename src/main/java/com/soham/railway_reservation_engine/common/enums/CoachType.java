package com.soham.railway_reservation_engine.common.enums;

/**
 * Classes of accommodation offered by a train (the "coach class" requested at booking time,
 * e.g. sleeper or AC 3-tier). Seats are allocated by scanning coaches of the requested type.
 */
public enum CoachType {
    SLEEPER,
    AC_3_TIER,
    AC_2_TIER,
    AC_1_TIER,
    GENERAL
}
