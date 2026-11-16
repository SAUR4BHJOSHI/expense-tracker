package com.captain.authservice.service;

import com.captain.authservice.entity.Role;
import com.captain.authservice.entity.UserCredential;
import com.captain.authservice.repository.RoleRepository;
import com.captain.authservice.repository.UserCredentialRepository;
import com.captain.authservice.dto.*;
import com.captain.authservice.exception.AuthException;
import com.captain.authservice.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Check if email already exists
        if (userCredentialRepository.existsByEmail(request.getEmail())) {
            throw AuthException.emailAlreadyExists();
        }

        // 2. Generate userId
        UUID userId = UUID.randomUUID();

        // 3. Hash password
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // 4. Assign default role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        // 5. Save credential
        UserCredential credential = UserCredential.builder()
                .userId(userId)
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .status("ACTIVE")
                .roles(Set.of(userRole))
                .build();

        userCredentialRepository.save(credential);

        // 6. Generate tokens
        List<String> roles = credential.getRoles().stream()
                .map(Role::getName)
                .toList();

        String accessToken = jwtService.generateAccessToken(userId, credential.getEmail(), roles);
        String refreshToken = jwtService.generateRefreshToken(userId);

        // 7. Store refresh token in Redis
        refreshTokenService.saveToken(refreshToken, userId);

        log.info("User registered successfully: {}", credential.getEmail());

        // TODO: Call User Service to create profile (Phase 4)
        // TODO: Publish UserRegisteredEvent to Kafka (Phase 6)

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userId)
                .email(credential.getEmail())
                .roles(roles)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Find credential by email
        UserCredential credential = userCredentialRepository.findByEmail(request.getEmail())
                .orElseThrow(AuthException::invalidCredentials);

        // 2. Check account status
        if (!"ACTIVE".equals(credential.getStatus())) {
            throw AuthException.accountLocked();
        }

        // 3. Verify password
        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw AuthException.invalidCredentials();
        }

        // 4. Generate tokens
        List<String> roles = credential.getRoles().stream()
                .map(Role::getName)
                .toList();

        String accessToken = jwtService.generateAccessToken(credential.getUserId(), credential.getEmail(), roles);
        String refreshToken = jwtService.generateRefreshToken(credential.getUserId());

        // 5. Store refresh token in Redis
        refreshTokenService.saveToken(refreshToken, credential.getUserId());

        log.info("User logged in: {}", credential.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(credential.getUserId())
                .email(credential.getEmail())
                .roles(roles)
                .build();
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String rawRefreshToken = request.getRefreshToken();

        // 1. Validate the refresh token is valid JWT
        if (!jwtService.isTokenValid(rawRefreshToken)) {
            throw AuthException.invalidToken();
        }

        // 2. Extract user info
        UUID userId = jwtService.extractUserId(rawRefreshToken);

        // 3. Validate and revoke old token in Redis
        refreshTokenService.validateAndRevoke(rawRefreshToken, userId);

        UserCredential credential = userCredentialRepository.findByUserId(userId)
                .orElseThrow(AuthException::invalidToken);

        // 4. Generate new token pair
        List<String> roles = credential.getRoles().stream()
                .map(Role::getName)
                .toList();

        String newAccessToken = jwtService.generateAccessToken(userId, credential.getEmail(), roles);
        String newRefreshToken = jwtService.generateRefreshToken(userId);

        // 5. Store new refresh token in Redis
        refreshTokenService.saveToken(newRefreshToken, userId);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(userId)
                .email(credential.getEmail())
                .roles(roles)
                .build();
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenService.revokeAllUserTokens(userId);
        log.info("User logged out, all tokens revoked: {}", userId);
    }

    public TokenValidationResponse validate(String token) {
        if (!jwtService.isTokenValid(token)) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .build();
        }

        return TokenValidationResponse.builder()
                .valid(true)
                .userId(jwtService.extractUserId(token))
                .email(jwtService.extractEmail(token))
                .roles(jwtService.extractRoles(token))
                .build();
    }
}

