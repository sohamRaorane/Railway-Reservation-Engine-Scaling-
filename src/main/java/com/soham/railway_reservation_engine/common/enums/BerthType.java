package com.soham.railway_reservation_engine.common.enums;

/**
 * Physical berth position of an allocated seat. RAC specifically uses SIDE_LOWER
 * berths (shared between two passengers).
 */
public enum BerthType {
    LOWER,

    MIDDLE,

    UPPER,

    SIDE_LOWER,

    SIDE_UPPER,


}
