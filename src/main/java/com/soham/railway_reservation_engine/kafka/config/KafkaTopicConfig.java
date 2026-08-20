package com.soham.railway_reservation_engine.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * Declares the Kafka topics this application owns.
 *
 * <p>Spring Boot's Kafka Admin API uses these {@code NewTopic} beans to auto-create the topics
 * at startup if they don't exist. {@code NewTopic(name, numPartitions, replicationFactor)} —
 * with 1 partition and replication factor 1 (single-broker development setup).
 *
 * <p><b>Event-driven architecture:</b> producers write events to these topics; consumers read
 * from them. Decoupling booking completion from side effects (promotion, notifications) makes
 * the booking transaction fast and lets those reactions scale independently.
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic bookingCreatedTopic(){
        return new NewTopic("booking.created" , 1 ,(short)1);
    }
    @Bean
    public NewTopic bookingCancelledTopic(){
        return new NewTopic("booking.cancelled" , 1 ,(short)1);
    }
}
