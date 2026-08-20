package com.soham.railway_reservation_engine.cancellation.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
/**
 * Computes the refundable amount for a cancellation based on time left before departure.
 *
 * <p><b>Business rule (time-window slabs):</b>
 * <table border="1">
 *   <tr><th>Time before departure</th><th>Refund</th></tr>
 *   <tr><td>&gt; 48 hours</td><td>75% of fare (25% charge)</td></tr>
 *   <tr><td>12–48 hours (inclusive of exactly 48h)</td><td>50% of fare (50% charge)</td></tr>
 *   <tr><td>&lt; 12 hours</td><td>0% (no refund)</td></tr>
 * </table>
 *
 * <p><b>Boundary care:</b> note the exact thresholds — {@code hoursRemaining > 48} gives 75%,
 * while {@code >= 12} gives 50%, so "exactly 48h" and "exactly 12h" both land in the 50% slab.
 * These edge cases are the classic source of boundary bugs and are pinned by
 * {@code ChargeCalculatorTest}. Duration arithmetic and timezone handling matter here — the app
 * pins the JVM to Asia/Kolkata to keep schedule times consistent.
 */
public class ChargeCalculator {

    public BigDecimal calculateRefund(
            BigDecimal totalFare,
            LocalDateTime journeryTime ,
            LocalDateTime cancellationTime
    ){
        long hoursRemaining = Duration.between(cancellationTime, journeryTime).toHours();

        //75% refund if cancelled more than 48 hours before departure
        if(hoursRemaining > 48 ){
            return totalFare.multiply(BigDecimal.valueOf(0.75))
                    .setScale(2, RoundingMode.HALF_EVEN);
        }

        if(hoursRemaining >= 12 ){
            return totalFare.multiply(BigDecimal.valueOf(0.50))
                    .setScale(2, RoundingMode.HALF_EVEN);
        }
        //for less than 12 hours
        return BigDecimal.ZERO.setScale(2);
    }
}
