package com.soham.railway_reservation_engine.config;

import com.soham.railway_reservation_engine.train.dto.TrainAvailabilityResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Declares the Redis templates used across the app.
 *
 * <p><b>Why two templates?</b> Redis itself is typeless (binary keys/values), so Spring Data Redis
 * needs a {@code RedisTemplate} to know how to (de)serialize. Serializers must match at write time
 * and read time — a mismatch silently returns {@code null}.
 * <ul>
 *   <li>{@code trainAvailabilityRedisTemplate} — values are JSON-serialized
 *       {@code TrainAvailabilityResponse} objects (cache-aside caching of availability lookups).</li>
 *   <li>{@code redisTemplate} (generic String/String) — lightweight values such as the 2-minute
 *       seat-hold TTL keys written by the seat-allocation strategy.</li>
 * </ul>
 *
 * <p>Note {@code JsonMapper.shared()} is a thread-safe, reusable Jackson mapper — creating mappers
 * per call would leak memory, so the template shares one instance.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, TrainAvailabilityResponse> trainAvailabilityRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {

        RedisTemplate<String, TrainAvailabilityResponse> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        ObjectMapper mapper = JsonMapper.shared();

        JacksonJsonRedisSerializer<TrainAvailabilityResponse> serializer =
                new JacksonJsonRedisSerializer<>(
                        mapper,
                        TrainAvailabilityResponse.class
                );

        template.setValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }

    //generic redis template for all the classes
    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory
    ){
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);

        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

}