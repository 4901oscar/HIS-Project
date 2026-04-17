package com.medflow.billing.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación sobre una factura
 * con un estado inválido (ej: pagar una factura ya pagada).
 * Mapea a HTTP 409 Conflict.
 */
public class InvalidInvoiceStatusException extends RuntimeException {
    
    public InvalidInvoiceStatusException(String message) {
        super(message);
    }
    
    public InvalidInvoiceStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
