package com.medflow.billing.exception;

/**
 * Excepción lanzada cuando no se encuentra una factura.
 * Mapea a HTTP 404 Not Found.
 */
public class InvoiceNotFoundException extends RuntimeException {
    
    public InvoiceNotFoundException(String message) {
        super(message);
    }
    
    public InvoiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
