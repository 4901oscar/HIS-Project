package com.medflow.auth.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("El correo electrónico ya está registrado: " + email);
    }
}
