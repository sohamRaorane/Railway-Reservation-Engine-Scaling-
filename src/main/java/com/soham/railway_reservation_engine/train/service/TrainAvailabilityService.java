package com.soham.railway_reservation_engine.train.service;

import com.soham.railway_reservation_engine.quotaReservationPool.entity.QuotaReservationPool;
import com.soham.railway_reservation_engine.quotaReservationPool.repository.QuotaReservationPoolRepository;
import com.soham.railway_reservation_engine.quotaSeatAllocation.repository.QuotaSeatAllocationRepository;
import com.soham.railway_reservation_engine.train.dto.TrainAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TrainAvailabilityService {
    private static final Logger log = LoggerFactory.getLogger(TrainAvailabilityService.class);
    private final QuotaSeatAllocationRepository quotaSeatAllocationRepository;
    private final QuotaReservationPoolRepository quotaReservationPoolRepository;
    private final RedisTemplate<String, TrainAvailabilityResponse> redisTemplate;

    public TrainAvailabilityResponse getAvailability(
           Long trainId,
           LocalDate journeyDate,
           String quota
    ) {
        //-----Starting the redis setup
        String cacheKey = "availability:" + trainId + ":" + journeyDate + ":" + quota;

        //Check the redis cache
        TrainAvailabilityResponse cachedData = redisTemplate.opsForValue().get(cacheKey);
        if(cachedData != null) {
            log.info("Availability served from Redis cache for key: {}", cacheKey);
            return cachedData;
        }

        //---- If not present in the redis cache, fetch from the database
        log.info("Availability cache miss, fetching from PostgreSQL for key: {}", cacheKey);

        Long availableSeats =
                quotaSeatAllocationRepository.getTotalAvailableSeats(
                        trainId,
                        journeyDate,
                        quota
                );

        QuotaReservationPool pool =
                quotaReservationPoolRepository.findPool(
                        trainId,
                        journeyDate,
                        quota
                ).orElseThrow(() ->
                        new RuntimeException("Quota reservation pool not found.")
                );

        TrainAvailabilityResponse response =
                new TrainAvailabilityResponse(
                        pool.getSchedule().getTrain().getNumber(),
                        pool.getSchedule().getTrain().getName(),
                        journeyDate,
                        quota,
                        availableSeats,
                        pool.getRacAvailable().longValue(),
                        pool.getWaitlistAvailable().longValue()
                );

        // Store the fetched data in the redis cache for future requests
        redisTemplate.opsForValue().set(cacheKey, response , Duration.ofMinutes(5));

        log.info("Availability stored in Redis cache for key: {}", cacheKey);

        return response;
    }
}
