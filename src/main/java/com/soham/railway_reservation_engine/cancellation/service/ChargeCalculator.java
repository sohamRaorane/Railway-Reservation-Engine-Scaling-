package com.soham.railway_reservation_engine.cancellation.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
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
