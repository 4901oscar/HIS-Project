package com.medframe.clinical.domain.exception;

import com.medframe.clinical.domain.service.PaymentValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando la validación de pago falla.
 * Esta excepción se mapea a HTTP 400 Bad Request.
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-5.6: Manejo de errores de validación de pago</li>
 *   <li>REQ-8.1-8.8: Formato de respuesta de error</li>
 * </ul>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentValidationException extends RuntimeException {
    
    private final PaymentValidationError errorCode;
    
    /**
     * Constructor con mensaje y código de error.
     * 
     * @param message mensaje de error en español
     * @param errorCode código de error específico
     */
    public PaymentValidationException(String message, PaymentValidationError errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructor legacy para compatibilidad.
     * 
     * @deprecated Use {@link #PaymentValidationException(String, PaymentValidationError)}
     */
    @Deprecated
    public PaymentValidationException(PaymentValidationError errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PaymentValidationError getErrorCode() {
        return errorCode;
    }
}