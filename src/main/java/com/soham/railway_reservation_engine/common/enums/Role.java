package com.soham.railway_reservation_engine.common.enums;

/**
 * User roles for RBAC. Spring Security maps these to authorities as {@code ROLE_USER} /
 * {@code ROLE_ADMIN} (see {@code AuthService} and {@code SecurityConfig}).
 */
public enum Role {
    USER,
    ADMIN,

}
