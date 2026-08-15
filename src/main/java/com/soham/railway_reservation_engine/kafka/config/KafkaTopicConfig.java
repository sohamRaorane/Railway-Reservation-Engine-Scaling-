package com.soham.railway_reservation_engine.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class KafkaTopicConfig {
    //these are the topics my application needs .create /manage them
    //when the spring starts
    //producers write events to these topics and consumers read events from these topics


    @Bean
    //create a kafka topic defination and register it in the spring application context
    //Spring Boot's Kafka infrastructure can then use Kafka's Admin API to create the topic if it doesn't already exist.

    public NewTopic bookingCreatedTopic(){
        return new NewTopic("booking.created" , 1 ,(short)1);
    }
    @Bean
    public NewTopic bookingCancelledTopic(){
        return new NewTopic("booking.cancelled" , 1 ,(short)1);
    }
}
