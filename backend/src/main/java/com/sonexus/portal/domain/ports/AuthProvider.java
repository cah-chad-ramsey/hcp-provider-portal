package com.sonexus.portal.domain.ports;

import com.sonexus.portal.domain.model.User;

import java.util.Optional;

/**
 * Port for authentication and user management.
 * Implementations: JwtAuthAdapter (local), OidcAuthAdapter (cloud/prod)
 */
public interface AuthProvider {

    /**
     * Authenticate user with credentials
     * @return JWT token or OIDC token depending on implementation
     */
    String authenticate(String email, String password);

    /**
     * Validate token and extract user information
     */
    Optional<User> validateToken(String token);

    /**
     * Register new user (may be disabled in production)
     */
    User registerUser(String email, String password, String firstName, String lastName);

    /**
     * Get current authenticated user
     */
    Optional<User> getCurrentUser();
}
