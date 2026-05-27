package com.medflow.billing.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler handler;
    private WebRequest request;
    
    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/billing/invoices/123");
    }
    
    @Test
    void handleInvoiceNotFound_shouldReturn404() {
        // Given
        InvoiceNotFoundException exception = new InvoiceNotFoundException("Factura no encontrada");
        
        // When
        ResponseEntity<ErrorResponse> response = handler.handleInvoiceNotFound(exception, request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("No Encontrado", response.getBody().getError());
        assertEquals("Factura no encontrada", response.getBody().getMessage());
        assertEquals("/api/billing/invoices/123", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }
    
    @Test
    void handleInvalidInvoiceStatus_shouldReturn409() {
        // Given
        InvalidInvoiceStatusException exception = 
            new InvalidInvoiceStatusException("Solo se pueden pagar facturas PENDIENTES");
        
        // When
        ResponseEntity<ErrorResponse> response = handler.handleInvalidInvoiceStatus(exception, request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflicto", response.getBody().getError());
        assertEquals("Solo se pueden pagar facturas PENDIENTES", response.getBody().getMessage());
        assertEquals("/api/billing/invoices/123", response.getBody().getPath());
    }
    
    @Test
    void handleInsufficientPayment_shouldReturn400() {
        // Given
        InsufficientPaymentException exception = 
            new InsufficientPaymentException("El monto pagado es insuficiente. Total: 100.00");
        
        // When
        ResponseEntity<ErrorResponse> response = handler.handleInsufficientPayment(exception, request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Solicitud Inválida", response.getBody().getError());
        assertEquals("El monto pagado es insuficiente. Total: 100.00", response.getBody().getMessage());
        assertEquals("/api/billing/invoices/123", response.getBody().getPath());
    }
    
    @Test
    void handleUnauthorized_shouldReturn403() {
        // Given
        UnauthorizedException exception = 
            new UnauthorizedException("No tiene permisos para acceder a esta factura");
        
        // When
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(exception, request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Prohibido", response.getBody().getError());
        assertEquals("No tiene permisos para acceder a esta factura", response.getBody().getMessage());
        assertEquals("/api/billing/invoices/123", response.getBody().getPath());
    }
    
    @Test
    void handleGenericException_shouldReturn500() {
        // Given
        Exception exception = new RuntimeException("Error inesperado");
        
        // When
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception, request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Error Interno del Servidor", response.getBody().getError());
        assertEquals("Ha ocurrido un error inesperado. Por favor, contacte al administrador.", 
                     response.getBody().getMessage());
        assertEquals("/api/billing/invoices/123", response.getBody().getPath());
    }
    
    @Test
    void errorResponse_shouldHaveAllFields() {
        // Given
        ErrorResponse error = new ErrorResponse(
            404,
            "No Encontrado",
            "Factura no encontrada",
            "/api/billing/invoices/123"
        );
        
        // Then
        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("No Encontrado", error.getError());
        assertEquals("Factura no encontrada", error.getMessage());
        assertEquals("/api/billing/invoices/123", error.getPath());
    }
    
    @Test
    void customExceptions_shouldSupportMessageAndCause() {
        // Test InvoiceNotFoundException
        Throwable cause = new RuntimeException("Root cause");
        InvoiceNotFoundException invoiceEx = new InvoiceNotFoundException("Message", cause);
        assertEquals("Message", invoiceEx.getMessage());
        assertEquals(cause, invoiceEx.getCause());
        
        // Test InvalidInvoiceStatusException
        InvalidInvoiceStatusException statusEx = new InvalidInvoiceStatusException("Message", cause);
        assertEquals("Message", statusEx.getMessage());
        assertEquals(cause, statusEx.getCause());
        
        // Test InsufficientPaymentException
        InsufficientPaymentException paymentEx = new InsufficientPaymentException("Message", cause);
        assertEquals("Message", paymentEx.getMessage());
        assertEquals(cause, paymentEx.getCause());
        
        // Test UnauthorizedException
        UnauthorizedException authEx = new UnauthorizedException("Message", cause);
        assertEquals("Message", authEx.getMessage());
        assertEquals(cause, authEx.getCause());
    }
}
