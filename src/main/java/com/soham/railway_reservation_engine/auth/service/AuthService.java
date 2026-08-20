package com.soham.railway_reservation_engine.auth.service;

import com.soham.railway_reservation_engine.auth.dto.LoginRequest;
import com.soham.railway_reservation_engine.auth.dto.LoginResponse;
import com.soham.railway_reservation_engine.auth.dto.RegisterRequest;
import com.soham.railway_reservation_engine.auth.dto.RegisterResponse;
import com.soham.railway_reservation_engine.common.enums.KycStatus;
import com.soham.railway_reservation_engine.common.enums.Role;
import com.soham.railway_reservation_engine.security.jwt.JwtService;
import com.soham.railway_reservation_engine.user.entity.User;
import com.soham.railway_reservation_engine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.soham.railway_reservation_engine.refreshToken.entity.RefreshToken;
import com.soham.railway_reservation_engine.refreshToken.service.RefreshTokenService;
import com.soham.railway_reservation_engine.auth.dto.RefreshRequest;
import com.soham.railway_reservation_engine.auth.dto.RefreshResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//Register a new user and login an existing user --> two responsibilities
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    //why this is taken beacuse
    //Authentication Manager -> CustomUserDetailsService -> Database -> Bcrpt -> Authenticated
    private final AuthenticationManager authenticationManager;


    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("User with email " + registerRequest.getEmail() + " already exists");
        }
        //otherwise a new user is registering
        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .gender(registerRequest.getGender())
                .dateOfBirth(registerRequest.getDateOfBirth())
                .role(Role.USER)
                .kycStatus(KycStatus.PENDING)
                .build();
        //save the user to the database
        userRepository.save(user);
        return RegisterResponse.builder()
                .message("User registered successfully")
                .build();
    }
    //Imp --> Spring Security already knows how to authenticate the user
    public LoginResponse login(LoginRequest loginRequest) {
        //so i need to check whether the user is authenticated or not
        //so we will use the authentication manager


        //returns an Authentication object if the authentication is successful, or throws an exception if it fails.
        authenticationManager.authenticate(
                //UsernamePasswordAuthenticationToken is a class that implements the Authentication interface
                // and is used to represent an authentication request with a username and password.
                //so the principal is email and the credential is the password
             //i am requesting authentication with these credentials
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        User  user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Accepts the UserDetails ---> not the user

        // Create a Spring Security UserDetails object
        //User detail requires three things --> username(email) , password , authorities
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()
                        )
                )
        );

        // Custom JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());

        // Generate JWT with custom claims
        String token = jwtService.generateToken(
                claims,
                userDetails
        );
        //Now the JWT is generated now create the refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public RefreshResponse refresh(RefreshRequest refreshRequest) {
        // Implementation for refreshing the access token using the provided refresh token
        //get the refresh token
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshRequest.getRefreshToken());

        //Handling the edge cases
        if(refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");

        }
        if(refreshTokenService.isExpired(refreshToken)) {
            throw new RuntimeException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        Collections.singletonList(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name()
                                )
                        )
                );
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());

        String accessToken = jwtService.generateToken(claims, userDetails);

        return RefreshResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    public void logout(RefreshRequest refreshRequest) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshRequest.getRefreshToken());
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

}