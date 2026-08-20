package com.soham.railway_reservation_engine.security.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Adapter between the {@code User} entity and Spring Security's {@code UserDetails} contract.
 *
 * <p><b>Why an adapter class?</b> Spring Security does not know our entity — it only understands
 * {@code UserDetails}. This wrapper adds the {@code userId} (needed by services to attribute
 * bookings) on top of the interface's required methods. The account flags all return true: the
 * app currently has no lockout/disable features, so no reason to refuse authentication on those
 * grounds.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails  implements UserDetails {
    private final Long userId;
    private final String username;
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}

