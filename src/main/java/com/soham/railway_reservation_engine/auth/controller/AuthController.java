package com.soham.railway_reservation_engine.auth.controller;


import com.soham.railway_reservation_engine.auth.dto.LoginRequest;
import com.soham.railway_reservation_engine.auth.dto.LoginResponse;
import com.soham.railway_reservation_engine.auth.dto.RegisterRequest;
import com.soham.railway_reservation_engine.auth.dto.RegisterResponse;
import com.soham.railway_reservation_engine.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.soham.railway_reservation_engine.auth.dto.RefreshRequest;
import com.soham.railway_reservation_engine.auth.dto.RefreshResponse;

/*
 * REST endpoints for authentication.
 *
 * Responsibilities of a controller: receive HTTP requests, convert JSON to DTOs,
 * delegate to the service layer, and return the service's response.
 * These endpoints are whitelisted in SecurityConfig (permitAll), so no JWT is
 * required to reach them.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        RegisterResponse registerResponse = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {

        LoginResponse loginResponse = authService.login(request);

        return ResponseEntity.ok(loginResponse);
    }
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @RequestBody RefreshRequest request
    ) {
        RefreshResponse refreshResponse = authService.refresh(request);
        return ResponseEntity.ok(refreshResponse);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody RefreshRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.ok("Logged out successfully");
    }

}
