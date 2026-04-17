package com.medflow.billing.exception;

/**
 * Excepción lanzada cuando el monto de pago es insuficiente para cubrir el total de la factura.
 * Mapea a HTTP 400 Bad Request.
 */
public class InsufficientPaymentException extends RuntimeException {
    
    public InsufficientPaymentException(String message) {
        super(message);
    }
    
    public InsufficientPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
