package com.sonexus.portal.api.controller;

import com.sonexus.portal.api.dto.*;
import com.sonexus.portal.domain.model.User;
import com.sonexus.portal.domain.ports.AuthProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthProvider authProvider;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            String token = authProvider.authenticate(request.getEmail(), request.getPassword());
            User user = authProvider.validateToken(token)
                    .orElseThrow(() -> new RuntimeException("Token generation failed"));

            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roles(user.getRoles())
                    .build();

            log.info("Login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register new user (development only)")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        try {
            User user = authProvider.registerUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName()
            );

            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roles(user.getRoles())
                    .createdAt(user.getCreatedAt())
                    .build();

            log.info("Registration successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.getEmail(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Current user", description = "Get current authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return authProvider.getCurrentUser()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(user.getRoles())
                        .createdAt(user.getCreatedAt())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }
}
