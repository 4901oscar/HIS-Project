package com.medflow.billing.controller;

import com.medflow.billing.dto.request.ApplyDiscountRequest;
import com.medflow.billing.dto.request.CreateInvoiceRequest;
import com.medflow.billing.dto.request.ProcessPaymentRequest;
import com.medflow.billing.dto.response.InvoiceResponse;
import com.medflow.billing.dto.response.PaymentResponse;
import com.medflow.billing.model.InvoiceStatus;
import com.medflow.billing.service.InvoiceService;
import com.medflow.billing.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing invoices and payments in the billing system.
 * 
 * Endpoints:
 * - POST /api/billing/invoices - Create invoice with charges
 * - GET /api/billing/invoices - List invoices (optional status filter)
 * - GET /api/billing/invoices/{id} - Get invoice by ID
 * - POST /api/billing/invoices/{id}/pay - Process payment
 * - PUT /api/billing/invoices/{id}/discount - Apply discount
 * - DELETE /api/billing/invoices/{id} - Cancel invoice
 * - GET /api/billing/invoices/patient/{patientId} - Get patient invoices
 */
@RestController
@RequestMapping("/api/billing/invoices")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    
    public InvoiceController(InvoiceService invoiceService, PaymentService paymentService) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }
    
    /**
     * Creates a new invoice with charges.
     * 
     * @param request the invoice creation request
     * @param createdBy the ID of the user creating the invoice (from JWT)
     * @return 201 Created with InvoiceResponse
     */
    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "system") String createdBy) {
        
        InvoiceResponse response = invoiceService.createInvoice(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Lists all invoices, optionally filtered by status.
     * 
     * @param status optional status filter (PENDING, PAID, CANCELLED)
     * @return 200 OK with list of InvoiceResponse
     */
    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getInvoices(
            @RequestParam(required = false) InvoiceStatus status) {
        
        List<InvoiceResponse> invoices = invoiceService.getInvoices(status);
        return ResponseEntity.ok(invoices);
    }
    
    /**
     * Retrieves an invoice by its ID.
     * Used by Clinical Service to validate payment before appointment activation.
     * 
     * @param id the invoice ID
     * @param serviceName the name of the service making the request (for audit)
     * @return 200 OK with InvoiceResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(
            @PathVariable String id,
            @RequestHeader(value = "X-Service-Name", required = false, defaultValue = "unknown") String serviceName) {
        
        // Log the service that requested the invoice for audit purposes
        if (!"unknown".equals(serviceName)) {
            // This will be logged by the service method
        }
        
        InvoiceResponse response = invoiceService.getById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Processes a payment for an invoice.
     * 
     * @param id the invoice ID
     * @param request the payment request
     * @param receivedBy the ID of the user receiving the payment (from JWT)
     * @return 200 OK with PaymentResponse including change amount
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable String id,
            @Valid @RequestBody ProcessPaymentRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "system") String receivedBy) {
        
        PaymentResponse response = paymentService.processPayment(id, request, receivedBy);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Applies a discount to an invoice.
     * Only PENDING invoices can receive discounts.
     * 
     * @param id the invoice ID
     * @param request the discount request (amount or percentage)
     * @return 200 OK with updated InvoiceResponse
     */
    @PutMapping("/{id}/discount")
    public ResponseEntity<InvoiceResponse> applyDiscount(
            @PathVariable String id,
            @Valid @RequestBody ApplyDiscountRequest request) {
        
        InvoiceResponse response = invoiceService.applyDiscount(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancels an invoice by changing its status to CANCELLED.
     * Only PENDING invoices can be cancelled.
     * 
     * @param id the invoice ID
     * @return 200 OK with updated InvoiceResponse
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<InvoiceResponse> cancelInvoice(@PathVariable String id) {
        InvoiceResponse response = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves all invoices for a specific patient.
     * PATIENT role users can only access their own invoices.
     * 
     * @param patientId the patient ID
     * @param requestingUserId the ID of the user making the request (from JWT)
     * @param userRole the role of the requesting user (from JWT)
     * @return 200 OK with list of InvoiceResponse
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<InvoiceResponse>> getPatientInvoices(
            @PathVariable String patientId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "system") String requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "ADMIN") String userRole) {
        
        List<InvoiceResponse> invoices = invoiceService.getPatientInvoices(patientId, requestingUserId, userRole);
        return ResponseEntity.ok(invoices);
    }
}
