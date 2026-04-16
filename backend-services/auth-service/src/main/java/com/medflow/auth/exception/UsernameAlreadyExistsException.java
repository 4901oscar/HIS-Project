package com.medflow.auth.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("El usuario ya está registrado: " + username);
    }
}
