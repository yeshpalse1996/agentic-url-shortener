
package com.agenticengineering.urlshortener.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import com.agenticengineering.urlshortener.dto.LoginRequest;
import com.agenticengineering.urlshortener.dto.RegisterRequest;
import com.agenticengineering.urlshortener.entity.AppUser;
import com.agenticengineering.urlshortener.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtEncoder jwtEncoder;
    private final AuthService authService;

    public AuthController(
            JwtEncoder jwtEncoder,
            AuthService authService) {
        this.jwtEncoder = jwtEncoder;
        this.authService = authService;
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresIn
    ) {}

    public record RegisterResponse(
            Long id,
            String username,
            String role,
            String message
    ) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {

        if (request.getUsername() == null
                || request.getUsername().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Username and password are required");
        }

        try {
            AppUser user = authService.register(
                    request.getUsername(),
                    request.getPassword());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getRole(),
                            "User registered successfully"));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        try {
            AppUser user = authService.authenticate(
                    request.username(),
                    request.password());

            Instant now = Instant.now();
            long expiresIn = 3600;

            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("agentic-url-shortener")
                    .subject(user.getUsername())
                    .claim("role", user.getRole())
                    .issuedAt(now)
                    .expiresAt(now.plus(expiresIn, ChronoUnit.SECONDS))
                    .build();

            String token = jwtEncoder
                    .encode(JwtEncoderParameters.from(
                            org.springframework.security.oauth2.jwt.JwsHeader
                                    .with(MacAlgorithm.HS256).build(),
                            claims))
                    .getTokenValue();

            return ResponseEntity.ok(
                    new LoginResponse(token, "Bearer", expiresIn));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }
    }
}