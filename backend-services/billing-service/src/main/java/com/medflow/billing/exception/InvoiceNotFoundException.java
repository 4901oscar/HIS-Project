package com.medflow.billing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando no se encuentra una factura.
 * Mapea a HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvoiceNotFoundException extends RuntimeException {
    
    public InvoiceNotFoundException(String message) {
        super(message);
    }
    
    public InvoiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
