package com.medflow.auth.controller;

import com.medflow.auth.domain.User;
import com.medflow.auth.dto.*;
import com.medflow.auth.repository.UserRepository;
import com.medflow.auth.service.AuthService;
import com.medflow.auth.service.JwtService;
import com.medflow.auth.service.LoginRateLimiter;
import com.medflow.auth.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenBlacklistService blacklistService;
    private final LoginRateLimiter rateLimiter;

    public AuthController(AuthService authService,
                          JwtService jwtService,
                          UserRepository userRepository,
                          TokenBlacklistService blacklistService,
                          LoginRateLimiter rateLimiter) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.blacklistService = blacklistService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        rateLimiter.checkRateLimit(ip);
        String token = authService.login(request.getUsername(), request.getPassword());
        rateLimiter.resetAttempts(ip);
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(new LoginResponse(token, jwtService.getExpirationTime(), UserResponse.fromUser(user)));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        log.info("[CU-00.2] Usuario registrado. Email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse("Cuenta creada exitosamente. Ya puedes iniciar sesion.", request.getEmail()));
    }

    @GetMapping("/activate")
    public ResponseEntity<Void> activate(@RequestParam String token) {
        authService.activate(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/create-patient")
    public ResponseEntity<CreatePatientAccountResponse> createPatientAccount(
            @Valid @RequestBody CreatePatientAccountRequest request) {
        CreatePatientAccountResponse response = authService.createPatientAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(extractToken(authHeader));
        return ResponseEntity.ok(new LogoutResponse("Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        if (blacklistService.isBlacklisted(token) || !jwtService.isValid(token)) {
            throw new RuntimeException("Invalid token");
        }
        User user = userRepository.findById(java.util.UUID.fromString(jwtService.extractUserId(token)))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(new RefreshResponse(jwtService.generateToken(user), jwtService.getExpirationTime()));
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestParam String token) {
        if (blacklistService.isBlacklisted(token) || !jwtService.isValid(token)) {
            return ResponseEntity.ok(new ValidateResponse(false, null, null, null));
        }
        return ResponseEntity.ok(new ValidateResponse(
                true, jwtService.extractUserId(token), jwtService.extractUsername(token), jwtService.extractRoles(token)
        ));
    }

    /** Obtener nombre completo de un usuario por ID. */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> getUserById(@PathVariable String id) {
        User user = userRepository.findById(java.util.UUID.fromString(id)).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("id", id, "fullName", user.getFullName()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        if (blacklistService.isBlacklisted(token) || !jwtService.isValid(token)) {
            throw new RuntimeException("Invalid token");
        }
        User user = userRepository.findById(java.util.UUID.fromString(jwtService.extractUserId(token)))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }


    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        String token = extractToken(authHeader);
        if (blacklistService.isBlacklisted(token) || !jwtService.isValid(token)) {
            throw new RuntimeException("Invalid token");
        }
        String userId = jwtService.extractUserId(token);
        authService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) return authHeader.substring(7);
        throw new RuntimeException("Invalid Authorization header");
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isEmpty()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}