package com.soham.railway_reservation_engine.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Smoke-test endpoint protected by the JWT filter: a successful response proves the
 * bearer token was issued, validated and injected by the security chain.
 */
@RestController
public class TestController {
    @GetMapping("/api/v1/test")
    public String test() {
        return "JWT Authentication Successful!";
    }
}
