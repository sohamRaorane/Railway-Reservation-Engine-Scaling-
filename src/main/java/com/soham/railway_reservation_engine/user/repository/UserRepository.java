package com.soham.railway_reservation_engine.user.repository;

import com.soham.railway_reservation_engine.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Data access for users. {@code findByEmail} powers login and JWT lookup;
 * {@code existsByEmail} implements the "email already registered" check at signup.
 * ({@code findByEmailAndPassword} is vestigial — authentication goes through
 * {@code AuthenticationManager} + BCrypt instead of raw password equality.)
 */
public interface UserRepository  extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndPassword(String email, String password);
    boolean existsByEmail(String email);
}
