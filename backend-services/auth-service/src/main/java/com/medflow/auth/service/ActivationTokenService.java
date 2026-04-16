package com.medflow.auth.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestión en memoria de tokens de activación (CU-00.2).
 * Expiran a las 24 horas y se consumen al activar.
 */
@Service
public class ActivationTokenService {

    private record TokenEntry(String userId, LocalDateTime expiresAt) {}

    private final Map<String, TokenEntry> tokens = new ConcurrentHashMap<>();

    public String generateToken(String userId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new TokenEntry(userId, LocalDateTime.now().plusHours(24)));
        return token;
    }

    /** Retorna el userId si el token es válido; null si expiró o no existe. */
    public String validateAndConsume(String token) {
        TokenEntry entry = tokens.remove(token);
        if (entry == null) return null;
        if (LocalDateTime.now().isAfter(entry.expiresAt())) return null;
        return entry.userId();
    }
}
