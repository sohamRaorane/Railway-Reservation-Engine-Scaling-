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

/**
 * Answers "how many seats are left on train X on date Y under quota Z?" using the
 * <b>cache-aside pattern</b>.
 *
 * <p><b>Flow:</b>
 * <ol>
 *   <li>Build the cache key ({@code availability:<trainId>:<date>:<quota>}) and check Redis.</li>
 *   <li>On a hit → return immediately (avoids two DB queries per request).</li>
 *   <li>On a miss → aggregate confirmed-seat availability from {@code QuotaSeatAllocation} and
 *       RAC/waitlist counters from {@code QuotaReservationPool}, then write the result to Redis
 *       with a 5-minute TTL so subsequent callers hit the cache.</li>
 * </ol>
 *
 * <p>This is the classic <i>read-through/cache-aside</i> trade: the 5-minute TTL means availability
 * can be up to 5 minutes stale — an acceptable price for shaving query load, since the DB is
 * always the source of truth at booking time.
 */
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
