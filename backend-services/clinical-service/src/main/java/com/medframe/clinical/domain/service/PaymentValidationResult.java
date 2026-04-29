package com.medframe.clinical.domain.service;

import com.medframe.clinical.infrastructure.client.dto.InvoiceResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Resultado de la validación de pago de una cita.
 * Contiene información sobre si la activación está permitida y detalles del error si aplica.
 */
@Data
@AllArgsConstructor
public class PaymentValidationResult {
    
    /**
     * Indica si la activación de la cita está permitida.
     */
    private boolean allowed;
    
    /**
     * Código de error si la validación falló.
     */
    private PaymentValidationError error;
    
    /**
     * Mensaje de error en español para mostrar al usuario.
     */
    private String errorMessage;
    
    /**
     * Respuesta de la factura si la validación fue exitosa.
     */
    private InvoiceResponse invoice;
    
    /**
     * Indica si hay una advertencia (por ejemplo, cita sin factura).
     */
    private boolean hasWarning;
    
    /**
     * Mensaje de advertencia en español.
     */
    private String warningMessage;
    
    /**
     * Crea un resultado exitoso con la factura validada.
     * 
     * @param invoice la factura con estado PAID
     * @return resultado exitoso
     */
    public static PaymentValidationResult success(InvoiceResponse invoice) {
        return new PaymentValidationResult(true, null, null, invoice, false, null);
    }
    
    /**
     * Crea un resultado permitido para citas sin factura (caso de compensación).
     * 
     * @return resultado permitido con advertencia
     */
    public static PaymentValidationResult allowedWithoutInvoice() {
        return new PaymentValidationResult(
            true, null, null, null, true, 
            "Cita activada sin factura asociada (caso de compensación)"
        );
    }
    
    /**
     * Crea un resultado permitido cuando la validación está deshabilitada.
     * 
     * @return resultado permitido con advertencia
     */
    public static PaymentValidationResult allowedWithoutValidation() {
        return new PaymentValidationResult(
            true, null, null, null, true, 
            "Validación de pago deshabilitada por configuración"
        );
    }
    
    /**
     * Crea un resultado fallido con código de error y mensaje.
     * 
     * @param error código de error
     * @param message mensaje de error en español
     * @return resultado fallido
     */
    public static PaymentValidationResult failed(PaymentValidationError error, String message) {
        return new PaymentValidationResult(false, error, message, null, false, null);
    }

    public static PaymentValidationResult failedWithInvoice(PaymentValidationError error, String message, InvoiceResponse invoice) {
        return new PaymentValidationResult(false, error, message, invoice, false, null);
    }
}
