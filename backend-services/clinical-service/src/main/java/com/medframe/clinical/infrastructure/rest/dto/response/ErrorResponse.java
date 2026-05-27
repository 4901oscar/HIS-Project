package com.medframe.clinical.infrastructure.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta de error estándar para la API.
 * 
 * <p>Incluye información detallada sobre el error para facilitar debugging
 * y proporcionar mensajes claros al cliente.</p>
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-8.1-8.8: Formato de respuesta de error con campos requeridos</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Timestamp del error en formato ISO-8601.
     */
    private LocalDateTime timestamp;
    
    /**
     * Código de estado HTTP (400, 404, 500, etc.).
     */
    private int status;
    
    /**
     * Nombre del error HTTP (Bad Request, Not Found, etc.).
     */
    private String error;
    
    /**
     * Código de error específico del dominio (opcional).
     * Ejemplos: PAYMENT_PENDING, INVOICE_CANCELLED, SERVICE_TIMEOUT
     */
    private String errorCode;
    
    /**
     * Mensaje de error en español para mostrar al usuario.
     */
    private String message;
    
    /**
     * Path de la request que generó el error.
     */
    private String path;
    
    /**
     * ID único de la request para tracking (opcional).
     */
    private String requestId;
    
    /**
     * Lista de errores de validación (opcional).
     * Usado principalmente para errores de validación de campos.
     */
    private List<String> errors;
    
    /**
     * Constructor legacy para mantener compatibilidad con código existente.
     * 
     * @deprecated Use el builder o constructor completo
     */
    @Deprecated
    public ErrorResponse(int status, String message, List<String> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = getHttpStatusName(status);
        this.message = message;
        this.errors = errors;
    }
    
    /**
     * Convierte código de estado HTTP a nombre legible.
     */
    private static String getHttpStatusName(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 422 -> "Unprocessable Entity";
            case 500 -> "Internal Server Error";
            case 503 -> "Service Unavailable";
            default -> "Error";
        };
    }
}
