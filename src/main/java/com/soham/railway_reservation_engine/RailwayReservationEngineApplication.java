package com.soham.railway_reservation_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RailwayReservationEngineApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

		System.out.println("ZoneId = " + java.time.ZoneId.systemDefault());
		System.out.println("user.timezone = " + System.getProperty("user.timezone"));

		SpringApplication.run(RailwayReservationEngineApplication.class, args);

	}

}
