package com.medflow.auth.exception;

public class InvalidActivationTokenException extends RuntimeException {
    public InvalidActivationTokenException() {
        super("El token de activación es inválido o ha expirado");
    }
}
