package com.soham.railway_reservation_engine.bookings.state;

import com.soham.railway_reservation_engine.common.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class PnrStateMachine {
    private final Map<BookingStatus , Set<BookingStatus>> transitionMap  = new EnumMap<>(BookingStatus.class);

    public PnrStateMachine(){
        //Confirm  --> Confirm , Cancel
        transitionMap.put(
              BookingStatus.CONFIRMED,
                EnumSet.of(
                        BookingStatus.CONFIRMED,
                        BookingStatus.CANCELLED
                )
        );
        //rac --> Confirm or Cancel
        transitionMap.put(
                BookingStatus.RAC,
                EnumSet.of(
                        BookingStatus.CONFIRMED,
                        BookingStatus.CANCELLED
                )
        );
        //Waitlist --> rac --> confirm --> cancel
        transitionMap.put(
                BookingStatus.WAITLIST,
                EnumSet.of(
                        BookingStatus.RAC,
                        BookingStatus.CONFIRMED,
                        BookingStatus.CANCELLED
                )
        );
        //cancel will remain cancel as it is
        transitionMap.put(
                BookingStatus.CANCELLED,
                EnumSet.noneOf(BookingStatus.class)
        );
    }
    public boolean canTransition(
            BookingStatus currentState,
            BookingStatus nextState
    ) {

        return transitionMap
                .getOrDefault(
                        currentState,
                        EnumSet.noneOf(BookingStatus.class)
                )
                .contains(nextState);
    }

    public void validateTransition(
            BookingStatus currentState,
            BookingStatus nextState
    ) {

        if (!canTransition(currentState, nextState)) {

            throw new IllegalStateException(
                    "Invalid PNR state transition from "
                            + currentState
                            + " to "
                            + nextState
            );
        }
    }

}
