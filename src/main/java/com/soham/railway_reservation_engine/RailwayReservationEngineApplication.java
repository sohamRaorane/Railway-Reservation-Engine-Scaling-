package com.soham.railway_reservation_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * Application entry point for the Railway Reservation Engine.
 *
 * <p>Annotations: {@code @SpringBootApplication} (component scan + auto-configuration),
 * {@code @ConfigurationPropertiesScan} (bind the {@code razorpay.*} properties into
 * {@code RazorpayProperties} without per-class annotations), and {@code @EnableScheduling}
 * (activates {@code ChartPreparationScheduler}).
 *
 * <p>The JVM default timezone is pinned to {@code Asia/Kolkata} before the context starts:
 * railway operations are India-local, and consistent handling of journey dates/refund windows
 * requires one canonical zone regardless of where the server is deployed.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class RailwayReservationEngineApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

		SpringApplication.run(RailwayReservationEngineApplication.class, args);

	}

}
