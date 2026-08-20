package com.soham.railway_reservation_engine.seat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Temporary seat-hold bookkeeping in Redis, keyed {@code seat_hold:<scheduleId>:<seatId>}.
 *
 * <p><b>Why a hold at all?</b> A booking is made, but payment happens LATER (user is redirected
 * to Razorpay). Without a hold, another customer could grab the same seat before payment lands.
 * The hold therefore reserves the seat for the payment window.
 *
 * <p><b>Why Redis + TTL instead of a DB column?</b> A TTL gives a self-expiring reservation for
 * free: if the user never pays, the key vanishes after {@value HOLD_DURATION} minutes and the seat
 * becomes bookable again — no cleanup job needed. {@code setIfAbsent} ({@code SET NX}) is atomic:
 * only the FIRST concurrent booker of a seat wins, making the check-and-set race-free.
 *
 * <p>On payment success/failure the booking flow deletes the hold explicitly
 * ({@code releaseSeat}); the TTL is only the fallback for abandoned payments.
 */
@Service
@RequiredArgsConstructor
public class SeatHoldService {
    private final StringRedisTemplate redisTemplate;
    //developing sathi --> 2mins
    private static final Duration  HOLD_DURATION = Duration.ofMinutes(2);

    //Building the key
    private String buildKey(Long scheduleId, Long seatId){
        return "seat_hold:" + scheduleId + ":" + seatId;
    }


    public boolean holdSeat(Long scheduleId, Long seatId, Long bookingId){
        String key = buildKey(scheduleId, seatId);
        //key will only be created when the key does not exsists
        //redis --> set NX --> set if not exists
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, bookingId.toString(), HOLD_DURATION);
        return Boolean.TRUE.equals(success);
    }

    public boolean isSeatHeld(Long scheduleId, Long seatId){
        String key = buildKey(scheduleId, seatId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void releaseSeat(Long scheduleId, Long seatId){
        String key = buildKey(scheduleId, seatId);
        redisTemplate.delete(key);

    }
    public String getHold(Long scheduleId, Long seatId){
        String key = buildKey(scheduleId, seatId);
        return redisTemplate.opsForValue().get(key);
    }
    public Long getRemainingTtl(Long scheduleId, Long seatId){
        Long ttl = redisTemplate.getExpire(buildKey(scheduleId, seatId));
        return ttl;
    }
}
