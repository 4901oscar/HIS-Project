package com.medflow.billing.service;

import com.medflow.billing.dto.request.ChargeRequest;
import com.medflow.billing.dto.request.CreateInvoiceRequest;
import com.medflow.billing.dto.response.ChargeResponse;
import com.medflow.billing.dto.response.InvoiceResponse;
import com.medflow.billing.model.Charge;
import com.medflow.billing.model.Invoice;
import com.medflow.billing.model.InvoiceStatus;
import com.medflow.billing.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing invoice operations.
 * Handles invoice creation, calculation, and business logic.
 */
@Service
@Slf4j
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    
    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }
    
    /**
     * Creates a new invoice with charges.
     * 
     * Steps:
     * 1. Generate invoice number in format INV-YYYYMMDD-XXXX (sequential per day)
     * 2. Calculate subtotal for each charge (quantity * unitPrice)
     * 3. Calculate total invoice subtotal (sum of all charge subtotals)
     * 4. Set initial status to PENDING
     * 5. Save invoice with charges
     * 6. Return InvoiceResponse
     * 
     * @param request the invoice creation request containing patient ID and charges
     * @param createdBy the ID of the user creating the invoice
     * @return InvoiceResponse with complete invoice details
     */
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request, String createdBy) {
        // 1. Generate invoice number
        String invoiceNumber = generateInvoiceNumber();
        
        // 2. Create invoice entity
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setPatientId(request.getPatientId());
        // REQ-1.8, REQ-5.4, REQ-7.3: Guardar appointmentId si estÃ¡ presente
        invoice.setAppointmentId(request.getAppointmentId());
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setCreatedBy(createdBy);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        
        // 3. Create charges and calculate subtotals
        List<Charge> charges = request.getCharges().stream()
            .map(chargeRequest -> createCharge(chargeRequest, invoice, createdBy))
            .collect(Collectors.toList());
        
        invoice.setCharges(charges);
        
        // 4. Calculate total invoice subtotal
        BigDecimal subtotal = charges.stream()
            .map(Charge::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        invoice.setSubtotal(subtotal);
        invoice.setTotal(subtotal); // Initially total = subtotal (no discount yet)
        
        // 5. Save invoice with charges
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // 6. Return InvoiceResponse
        return mapToResponse(savedInvoice);
    }
    
    /**
     * Generates a unique invoice number in format INV-YYYYMMDD-XXXX.
     * The XXXX part is sequential per day.
     * 
     * @return generated invoice number
     */
    private String generateInvoiceNumber() {
        LocalDateTime now = LocalDateTime.now();
        String datePrefix = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String invoicePrefix = "INV-" + datePrefix + "-";
        
        // Find the last invoice number for today
        List<Invoice> todayInvoices = invoiceRepository.findAll().stream()
            .filter(inv -> inv.getInvoiceNumber().startsWith(invoicePrefix))
            .collect(Collectors.toList());
        
        int nextSequence = todayInvoices.size() + 1;
        
        // Format sequence as 4-digit number with leading zeros
        return String.format("%s%04d", invoicePrefix, nextSequence);
    }
    
    /**
     * Creates a Charge entity from a ChargeRequest.
     * Calculates the subtotal as quantity * unitPrice.
     * 
     * @param chargeRequest the charge request data
     * @param invoice the parent invoice
     * @return created Charge entity
     */
    private Charge createCharge(ChargeRequest chargeRequest, Invoice invoice, String createdBy) {
        Charge charge = new Charge();
        charge.setInvoice(invoice);
        charge.setType(chargeRequest.getType());
        charge.setDescription(chargeRequest.getDescription());
        charge.setQuantity(chargeRequest.getQuantity());
        charge.setUnitPrice(chargeRequest.getUnitPrice());
        
        // Calculate subtotal: quantity * unitPrice
        BigDecimal subtotal = chargeRequest.getUnitPrice()
            .multiply(BigDecimal.valueOf(chargeRequest.getQuantity()));
        charge.setSubtotal(subtotal);
        charge.setCreatedBy(createdBy);
        
        return charge;
    }
    
    /**
     * Maps an Invoice entity to an InvoiceResponse DTO.
     * 
     * @param invoice the invoice entity
     * @return InvoiceResponse DTO
     */
    private InvoiceResponse mapToResponse(Invoice invoice) {
        List<ChargeResponse> chargeResponses = invoice.getCharges().stream()
            .map(this::mapChargeToResponse)
            .collect(Collectors.toList());
        
        return new InvoiceResponse(
            invoice.getId(),
            invoice.getInvoiceNumber(),
            invoice.getPatientId(),
            invoice.getAppointmentId(), // REQ-7.6, REQ-8.3: Incluir appointmentId en respuesta
            chargeResponses,
            invoice.getSubtotal(),
            invoice.getDiscountAmount(),
            invoice.getTotal(),
            invoice.getStatus(),
            invoice.getCreatedAt(),
            invoice.getCreatedBy(),
            invoice.getCustomerNit(),
            invoice.getCustomerName(),
            invoice.getUpdatedAt()
        );
    }
    
    /**
     * Retrieves all invoices, optionally filtered by status.
     * 
     * @param status optional status filter (PENDING, PAID, CANCELLED). If null, returns all invoices.
     * @return list of InvoiceResponse DTOs
     */
    public List<InvoiceResponse> getInvoices(InvoiceStatus status) {
        List<Invoice> invoices;
        
        if (status != null) {
            // Filter by status if provided
            invoices = invoiceRepository.findByStatus(status);
        } else {
            // Return all invoices if no status filter
            invoices = invoiceRepository.findAll();
        }
        
        // Map to response DTOs
        return invoices.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Maps a Charge entity to a ChargeResponse DTO.
     * 
     * @param charge the charge entity
     * @return ChargeResponse DTO
     */
    private ChargeResponse mapChargeToResponse(Charge charge) {
        return new ChargeResponse(
            charge.getId(),
            charge.getType(),
            charge.getDescription(),
            charge.getQuantity(),
            charge.getUnitPrice(),
            charge.getSubtotal()
        );
    }
    
    /**
     * Retrieves an invoice by its ID.
     * 
     * @param id the invoice ID
     * @return InvoiceResponse DTO
     * @throws com.medflow.billing.exception.InvoiceNotFoundException if invoice not found
     */
    public InvoiceResponse getById(String id) {
        log.info("Getting invoice by ID: {}", id);
        
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new com.medflow.billing.exception.InvoiceNotFoundException(
                "Factura no encontrada con ID: " + id));
        
        log.info("Invoice {} retrieved successfully with status: {}", id, invoice.getStatus());
        return mapToResponse(invoice);
    }
    
    /**
     * Applies a discount to an invoice.
     * Only PENDING invoices can receive discounts (BR2).
     * Recalculates total as subtotal - discountAmount.
     * Total never goes negative (BR7).
     * 
     * @param id the invoice ID
     * @param request the discount request (either fixed amount or percentage)
     * @return updated InvoiceResponse
     * @throws com.medflow.billing.exception.InvoiceNotFoundException if invoice not found
     * @throws com.medflow.billing.exception.InvalidInvoiceStatusException if invoice is not PENDING
     */
    @Transactional
    public InvoiceResponse applyDiscount(String id, com.medflow.billing.dto.request.ApplyDiscountRequest request, String userId) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new com.medflow.billing.exception.InvoiceNotFoundException(
                "Factura no encontrada con ID: " + id));
        
        // BR2: Only PENDING invoices can receive discounts
        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new com.medflow.billing.exception.InvalidInvoiceStatusException(
                "Solo se pueden aplicar descuentos a facturas PENDIENTES");
        }
        
        // Calculate discount amount
        BigDecimal discountAmount;
        if (request.getDiscountAmount() != null) {
            // Fixed discount amount
            discountAmount = request.getDiscountAmount();
        } else if (request.getDiscountPercentage() != null) {
            // Percentage discount
            discountAmount = invoice.getSubtotal()
                .multiply(request.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else {
            throw new IllegalArgumentException("Debe proporcionar un monto o porcentaje de descuento");
        }
        
        // BR7: Total never negative - cap discount at subtotal
        if (discountAmount.compareTo(invoice.getSubtotal()) > 0) {
            discountAmount = invoice.getSubtotal();
        }
        
        // Update invoice
        invoice.setDiscountAmount(discountAmount);
        invoice.setTotal(invoice.getSubtotal().subtract(discountAmount));
        invoice.setUpdatedBy(userId != null ? userId : "internal");
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToResponse(savedInvoice);
    }
    
    /**
     * Cancels an invoice by changing its status to CANCELLED.
     * Only PENDING invoices can be cancelled (BR2).
     * 
     * @param id the invoice ID
     * @return updated InvoiceResponse
     * @throws com.medflow.billing.exception.InvoiceNotFoundException if invoice not found
     * @throws com.medflow.billing.exception.InvalidInvoiceStatusException if invoice is not PENDING
     */
    @Transactional
    public InvoiceResponse cancelInvoice(String id, String userId) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new com.medflow.billing.exception.InvoiceNotFoundException(
                "Factura no encontrada con ID: " + id));
        
        // BR2: Only PENDING invoices can be cancelled
        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new com.medflow.billing.exception.InvalidInvoiceStatusException(
                "Solo se pueden cancelar facturas PENDIENTES");
        }
        
        // Update status to CANCELLED
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setUpdatedBy(userId != null ? userId : "internal");
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToResponse(savedInvoice);
    }
    
    /**
     * Retrieves all invoices for a specific patient.
     * Validates that PATIENT role users can only access their own invoices (BR4).
     * 
     * @param patientId the patient ID
     * @param requestingUserId the ID of the user making the request
     * @param userRole the role of the requesting user (PATIENT, CASHIER, ADMIN)
     * @return list of InvoiceResponse DTOs
     * @throws SecurityException if PATIENT role tries to access another patient's invoices
     */
    public List<InvoiceResponse> getPatientInvoices(String patientId, String requestingUserId, String userRole) {
        // BR4: Patient can only access their own invoices
        if ("PATIENT".equals(userRole) && !patientId.equals(requestingUserId)) {
            throw new SecurityException("No tiene permiso para ver las facturas de otro paciente");
        }
        
        List<Invoice> invoices = invoiceRepository.findByPatientId(patientId);
        
        return invoices.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
}
