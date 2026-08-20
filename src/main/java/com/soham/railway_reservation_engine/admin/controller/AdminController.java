package com.soham.railway_reservation_engine.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only endpoints.
 *
 * <p>Demonstrates RBAC (Role-Based Access Control): this controller lives under
 * the {@code /api/v1/admin/**} path, and {@code SecurityConfig} guards that path
 * with {@code .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")}. Access is
 * denied unless the JWT carries a {@code ROLE_ADMIN} authority, which Spring
 * derives from the {@code role} claim set at login. Every other endpoint in the
 * app is only protected by authentication, so this is the simplest example of
 * role-level authorisation in the codebase.
 */
@RestController
public class AdminController {

    @GetMapping("/api/v1/admin/test")
    public String adminTest(){
        return "Welcome Admin";
    }
}
