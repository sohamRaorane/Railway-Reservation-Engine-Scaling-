package com.soham.railway_reservation_engine.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Liveness endpoint — used to verify the application has started and is serving traffic.
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Railway Reservation Engine is Running!";
    }
}