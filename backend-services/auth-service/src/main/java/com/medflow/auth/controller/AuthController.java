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

    /** CU-00.1: Login por username o correo. */
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

    /** CU-00.2: Auto-registro de paciente desde el portal web. */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        String activationToken = authService.register(request);
        log.info("[CU-00.2] Usuario registrado. Token de activación generado para: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(
                        "Registro exitoso. Revisa tu correo para activar tu cuenta.",
                        request.getEmail()
                ));
    }

    /** CU-00.2: Activación de cuenta por link de correo. */
    @GetMapping("/activate")
    public ResponseEntity<Void> activate(@RequestParam String token) {
        authService.activate(token);
        return ResponseEntity.ok().build();
    }

    /** CU-01: Admisión crea cuenta de paciente (llamado por patient-service). */
    @PostMapping("/internal/create-patient")
    public ResponseEntity<CreatePatientAccountResponse> createPatientAccount(
            @Valid @RequestBody CreatePatientAccountRequest request) {
        CreatePatientAccountResponse response = authService.createPatientAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Logout. */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(extractToken(authHeader));
        return ResponseEntity.ok(new LogoutResponse("Logged out successfully"));
    }

    /** Refresh token. */
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

    /** Validar token. */
    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestParam String token) {
        if (blacklistService.isBlacklisted(token) || !jwtService.isValid(token)) {
            return ResponseEntity.ok(new ValidateResponse(false, null, null, null));
        }
        return ResponseEntity.ok(new ValidateResponse(
                true,
                jwtService.extractUserId(token),
                jwtService.extractUsername(token),
                jwtService.extractRoles(token)
        ));
    }

    /** Datos del usuario autenticado. */
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

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) return authHeader.substring(7);
        throw new RuntimeException("Invalid Authorization header");
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isEmpty()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}
