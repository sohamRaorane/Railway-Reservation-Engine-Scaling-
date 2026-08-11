package com.soham.railway_reservation_engine.booking.stateMachine;

import com.soham.railway_reservation_engine.bookings.state.PnrStateMachine;
import com.soham.railway_reservation_engine.common.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PnrStateMachineTest {

    private PnrStateMachine pnrStateMachine;

    @BeforeEach
    void setUp() {
        pnrStateMachine = new PnrStateMachine();
    }
    @Test
    void shouldRejectCancelledToConfirmedTransition() {
        assertThrows(
                IllegalStateException.class,
                () -> pnrStateMachine.validateTransition(
                        BookingStatus.CANCELLED,
                        BookingStatus.CONFIRMED
                )
        );
    }

    @Test
    void shouldAllowWaitlistedToConfirmedTransition() {
        assertDoesNotThrow(
                () -> pnrStateMachine.validateTransition(
                        BookingStatus.WAITLIST,
                        BookingStatus.CONFIRMED
                )
        );
    }

}


