package com.soham.railway_reservation_engine.cancellation.service;

import com.soham.railway_reservation_engine.refund.entity.Refund;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;


//Boundary table just for refernece
/*
| Time before Departure | Refund  | Cancellation Charge |
        | --------------------- | ------- | ------------------: |
        | **48 hr + 1 min**     | **75%** |                 25% |
        | **Exactly 48 hr**     | **50%** |                 50% |
        | **47 hr 59 min**      | **50%** |                 50% |
        | **12 hr + 1 min**     | **50%** |                 50% |
        | **Exactly 12 hr**     | **50%** |                 50% |
        | **11 hr 59 min**      | **0%**  |                100% |
 */



public class ChargeCalculatorTest {
    private ChargeCalculator chargeCalculator ;

    @BeforeEach
    public void setUp(){
        chargeCalculator = new ChargeCalculator();
    }
    //More than 48 hrs
    @Test
    void shouldReturn75PercentRefundWhenCancelledMoreThan48HoursBeforeDeparture() {
        // Implement test logic here
        BigDecimal refund = chargeCalculator.calculateRefund(
                BigDecimal.valueOf(1000),
                //journery date --> 10/8/2024 10:00 AM
                LocalDateTime.of(2024 , 8,10 , 10, 0),
                //cancellation date -->
                LocalDateTime.of(2024, 8 , 7 , 9 , 59)

        );
        assertEquals(BigDecimal.valueOf(750).setScale(2), refund);
    }

    @Test
    void shouldReturn50PercentRefundWhenCancelledExactly48HoursBeforeDeparture() {

        BigDecimal refund = chargeCalculator.calculateRefund(
                BigDecimal.valueOf(1000),
                LocalDateTime.of(2026, 8, 10, 10, 0),
                LocalDateTime.of(2026, 8, 8, 10, 0)
        );

        assertEquals(
                new BigDecimal("500.00"),
                refund
        );
    }

    @Test
    void shouldReturn50PercentRefundWhenCancelled47Hours59MinutesBeforeDeparture() {

        BigDecimal refund = chargeCalculator.calculateRefund(
                BigDecimal.valueOf(1000),
                LocalDateTime.of(2026, 8, 10, 10, 0),
                LocalDateTime.of(2026, 8, 8, 10, 1)
        );

        assertEquals(
                new BigDecimal("500.00"),
                refund
        );
    }

    @Test
    void shouldReturn50PercentRefundWhenCancelled12Hours1MinuteBeforeDeparture() {

        BigDecimal refund = chargeCalculator.calculateRefund(
                BigDecimal.valueOf(1000),
                LocalDateTime.of(2026, 8, 10, 10, 0),
                LocalDateTime.of(2026, 8, 9, 21, 59)
        );

        assertEquals(
                new BigDecimal("500.00"),
                refund
        );
    }
    @Test
    void shouldReturn50PercentRefundWhenCancelledExactly12HoursBeforeDeparture() {

        BigDecimal refund = chargeCalculator.calculateRefund(
                BigDecimal.valueOf(1000),
                LocalDateTime.of(2026, 8, 10, 10, 0),
                LocalDateTime.of(2026, 8, 9, 22, 0)
        );

        assertEquals(
                new BigDecimal("500.00"),
                refund
        );
    }
    @Test
    void shouldReturnZeroRefundWhenCancelledLessThan12HoursBeforeDeparture() {

        BigDecimal refund = chargeCalculator.calculateRefund(
                BigDecimal.valueOf(1000),
                LocalDateTime.of(2026, 8, 10, 10, 0),
                LocalDateTime.of(2026, 8, 9, 22, 1)
        );

        assertEquals(
                new BigDecimal("0.00"),
                refund
        );
    }
}
