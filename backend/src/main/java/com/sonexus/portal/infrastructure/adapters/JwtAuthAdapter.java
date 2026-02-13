package com.sonexus.portal.infrastructure.adapters;

import com.sonexus.portal.domain.model.User;
import com.sonexus.portal.domain.ports.AuthProvider;
import com.sonexus.portal.infrastructure.persistence.entity.RoleEntity;
import com.sonexus.portal.infrastructure.persistence.entity.UserEntity;
import com.sonexus.portal.infrastructure.persistence.repository.RoleRepository;
import com.sonexus.portal.infrastructure.persistence.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Profile({"default", "local", "test"})
@Slf4j
public class JwtAuthAdapter implements AuthProvider {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey secretKey;
    private final long jwtExpiration;

    public JwtAuthAdapter(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.jwt.secret}") String jwtSecret,
            @Value("${app.security.jwt.expiration}") long jwtExpiration) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpiration = jwtExpiration;
    }

    @Override
    public String authenticate(String email, String password) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, userEntity.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        return generateToken(userEntity);
    }

    @Override
    public Optional<User> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = claims.get("userId", Long.class);
            UserEntity userEntity = userRepository.findById(userId)
                    .orElse(null);

            if (userEntity == null) {
                return Optional.empty();
            }

            return Optional.of(mapToUser(userEntity));
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return Optional.empty();
        }
    }

    @Override
    public User registerUser(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        RoleEntity officeStaffRole = roleRepository.findByName("OFFICE_STAFF")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .roles(Set.of(officeStaffRole))
                .build();

        UserEntity saved = userRepository.save(userEntity);
        return mapToUser(saved);
    }

    @Override
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(this::mapToUser);
    }

    private String generateToken(UserEntity userEntity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        List<String> roles = userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(userEntity.getEmail())
                .claim("userId", userEntity.getId())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    private User mapToUser(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .roles(entity.getRoles().stream()
                        .map(RoleEntity::getName)
                        .collect(Collectors.toSet()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
