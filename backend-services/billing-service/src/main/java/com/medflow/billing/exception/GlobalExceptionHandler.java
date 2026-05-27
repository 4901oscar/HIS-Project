package com.medflow.billing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.context.request.WebRequest;

/**
 * Manejador global de excepciones para el servicio de facturaciÃ³n.
 * Todos los mensajes de error estÃ¡n en espaÃ±ol segÃºn BR6.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Maneja InvoiceNotFoundException - 404 Not Found
     */
    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceNotFound(
            InvoiceNotFoundException ex, 
            WebRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "No Encontrado",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Maneja InvalidInvoiceStatusException - 409 Conflict
     */
    @ExceptionHandler(InvalidInvoiceStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInvoiceStatus(
            InvalidInvoiceStatusException ex, 
            WebRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Conflicto",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    /**
     * Maneja InsufficientPaymentException - 400 Bad Request
     */
    @ExceptionHandler(InsufficientPaymentException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPayment(
            InsufficientPaymentException ex, 
            WebRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Solicitud InvÃ¡lida",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Maneja UnauthorizedException - 403 Forbidden
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, 
            WebRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Prohibido",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Maneja excepciones genÃ©ricas - 500 Internal Server Error
     */
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            WebRequest request) {

        String detail = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        String message = "Datos duplicados o invalidos.";
        if (detail != null && detail.contains("code")) {
            message = "Ya existe un servicio con ese codigo.";
        } else if (detail != null && detail.contains("invoice_number")) {
            message = "Ya existe una factura con ese numero.";
        }

        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Conflicto",
            message,
            request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
@ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, 
            WebRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error Interno del Servidor",
            "Ha ocurrido un error inesperado. Por favor, contacte al administrador.",
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
