package com.soham.railway_reservation_engine.security.service;

import com.soham.railway_reservation_engine.user.entity.User;
import com.soham.railway_reservation_engine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Bridges Spring Security's authentication layer to our {@code User} table.
 *
 * <p>Called by Spring Security (and by {@code JwtFilter}) with a username; it resolves the user
 * from the DB and wraps it as {@code CustomUserDetails}, mapping the user's {@code Role} to a
 * {@code ROLE_*} authority string — the format Spring's {@code hasRole(...)} expressions expect.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + username
                        ));

        return new CustomUserDetails(

                user.getId(),

                user.getEmail(),

                user.getPassword(),

                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()
                        )
                )

        );
    }
}