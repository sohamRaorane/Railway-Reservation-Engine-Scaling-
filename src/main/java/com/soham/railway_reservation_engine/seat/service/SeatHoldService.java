package com.soham.railway_reservation_engine.seat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

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
