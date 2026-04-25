package com.medflow.billing.service;

import com.medflow.billing.dto.request.ProcessPaymentRequest;
import com.medflow.billing.dto.response.PaymentResponse;
import com.medflow.billing.exception.InsufficientPaymentException;
import com.medflow.billing.exception.InvalidInvoiceStatusException;
import com.medflow.billing.exception.InvoiceNotFoundException;
import com.medflow.billing.model.Invoice;
import com.medflow.billing.model.InvoiceStatus;
import com.medflow.billing.model.Payment;
import com.medflow.billing.repository.InvoiceRepository;
import com.medflow.billing.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service class for managing payment operations.
 * Handles payment processing and business logic.
 */
@Service
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    
    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }
    
    /**
     * Processes a payment for an invoice.
     * 
     * Business Rules:
     * - BR1: Only PENDING invoices can be paid
     * - BR3: Payment amount must be >= invoice total
     * 
     * Steps:
     * 1. Validate invoice exists
     * 2. Validate invoice status is PENDING
     * 3. Validate payment amount >= invoice total
     * 4. Create payment record
     * 5. Update invoice status to PAID
     * 6. Calculate change
     * 7. Return payment response
     * 
     * @param invoiceId the ID of the invoice to pay
     * @param request the payment request containing amount and method
     * @param receivedBy the ID of the user receiving the payment
     * @return PaymentResponse with payment details including change
     * @throws InvoiceNotFoundException if invoice not found
     * @throws InvalidInvoiceStatusException if invoice is not PENDING
     * @throws InsufficientPaymentException if payment amount < invoice total
     */
    @Transactional
    public PaymentResponse processPayment(String invoiceId, ProcessPaymentRequest request, String receivedBy) {
        // 1. Validate invoice exists
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException("Factura no encontrada con ID: " + invoiceId));
        
        // 2. BR1: Validate invoice status is PENDING
        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new InvalidInvoiceStatusException(
                "Solo se pueden pagar facturas PENDIENTES. Estado actual: " + invoice.getStatus());
        }
        
        // 3. BR3: Validate payment amount >= invoice total
        if (request.getAmount().compareTo(invoice.getTotal()) < 0) {
            throw new InsufficientPaymentException(
                String.format("El monto pagado (%.2f) es insuficiente. Total de la factura: %.2f",
                    request.getAmount(), invoice.getTotal()));
        }
        
        // 4. Calculate change
        BigDecimal change = request.getAmount().subtract(invoice.getTotal());
        
        // 5. Create payment record
        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setAmount(request.getAmount());
        payment.setChange(change);
        payment.setMethod(request.getMethod());
        payment.setPaidAt(LocalDateTime.now());
        payment.setReceivedBy(receivedBy);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // 6. Update invoice with customer billing information and status to PAID
        invoice.setCustomerNit(request.getNit());
        invoice.setCustomerName(request.getCustomerName());
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setUpdatedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
        
        // 7. Return payment response
        return mapToResponse(savedPayment);
    }
    
    /**
     * Maps a Payment entity to a PaymentResponse DTO.
     * 
     * @param payment the payment entity
     * @return PaymentResponse DTO
     */
    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getInvoiceId(),
            payment.getAmount(),
            payment.getChange(),
            payment.getMethod(),
            payment.getPaidAt(),
            payment.getReceivedBy()
        );
    }
}
