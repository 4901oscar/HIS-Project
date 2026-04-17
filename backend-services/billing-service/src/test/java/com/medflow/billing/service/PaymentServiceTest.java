package com.medflow.billing.service;

import com.medflow.billing.dto.request.ProcessPaymentRequest;
import com.medflow.billing.dto.response.PaymentResponse;
import com.medflow.billing.exception.InsufficientPaymentException;
import com.medflow.billing.exception.InvalidInvoiceStatusException;
import com.medflow.billing.exception.InvoiceNotFoundException;
import com.medflow.billing.model.Invoice;
import com.medflow.billing.model.InvoiceStatus;
import com.medflow.billing.model.Payment;
import com.medflow.billing.model.PaymentMethod;
import com.medflow.billing.repository.InvoiceRepository;
import com.medflow.billing.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService.
 * Tests payment processing, validation, and business rules.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private InvoiceRepository invoiceRepository;
    
    @InjectMocks
    private PaymentService paymentService;
    
    private Invoice pendingInvoice;
    private ProcessPaymentRequest validPaymentRequest;
    private String receivedBy;
    
    @BeforeEach
    void setUp() {
        receivedBy = "cashier-123";
        
        // Create a pending invoice
        pendingInvoice = new Invoice();
        pendingInvoice.setId("inv-1");
        pendingInvoice.setInvoiceNumber("INV-20260416-0001");
        pendingInvoice.setPatientId("patient-1");
        pendingInvoice.setSubtotal(new BigDecimal("100.00"));
        pendingInvoice.setDiscountAmount(BigDecimal.ZERO);
        pendingInvoice.setTotal(new BigDecimal("100.00"));
        pendingInvoice.setStatus(InvoiceStatus.PENDING);
        pendingInvoice.setCreatedAt(LocalDateTime.now());
        pendingInvoice.setCreatedBy("user-1");
        
        // Create a valid payment request
        validPaymentRequest = new ProcessPaymentRequest();
        validPaymentRequest.setAmount(new BigDecimal("100.00"));
        validPaymentRequest.setMethod(PaymentMethod.CASH);
    }
    
    @Test
    void processPayment_shouldCreatePaymentAndUpdateInvoice_whenPaymentIsExact() {
        // Arrange
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(pendingInvoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId("payment-1");
            return payment;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        PaymentResponse response = paymentService.processPayment("inv-1", validPaymentRequest, receivedBy);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("payment-1");
        assertThat(response.getInvoiceId()).isEqualTo("inv-1");
        assertThat(response.getAmount()).isEqualByComparingTo("100.00");
        assertThat(response.getChange()).isEqualByComparingTo("0.00");
        assertThat(response.getMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(response.getReceivedBy()).isEqualTo("cashier-123");
        assertThat(response.getPaidAt()).isNotNull();
        
        verify(paymentRepository).save(any(Payment.class));
        verify(invoiceRepository).save(any(Invoice.class));
    }
    
    @Test
    void processPayment_shouldCalculateChange_whenPaymentExceedsTotal() {
        // Arrange
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(pendingInvoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId("payment-2");
            return payment;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ProcessPaymentRequest overpaymentRequest = new ProcessPaymentRequest();
        overpaymentRequest.setAmount(new BigDecimal("150.00"));
        overpaymentRequest.setMethod(PaymentMethod.CASH);
        
        // Act
        PaymentResponse response = paymentService.processPayment("inv-1", overpaymentRequest, receivedBy);
        
        // Assert
        assertThat(response.getAmount()).isEqualByComparingTo("150.00");
        assertThat(response.getChange()).isEqualByComparingTo("50.00"); // 150 - 100
    }
    
    @Test
    void processPayment_shouldUpdateInvoiceStatusToPaid() {
        // Arrange
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(pendingInvoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId("payment-3");
            return payment;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        
        // Act
        paymentService.processPayment("inv-1", validPaymentRequest, receivedBy);
        
        // Assert
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice savedInvoice = invoiceCaptor.getValue();
        
        assertThat(savedInvoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(savedInvoice.getUpdatedAt()).isNotNull();
    }
    
    @Test
    void processPayment_shouldThrowException_whenInvoiceNotFound() {
        // Arrange
        when(invoiceRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment("non-existent", validPaymentRequest, receivedBy))
            .isInstanceOf(InvoiceNotFoundException.class)
            .hasMessageContaining("Factura no encontrada con ID: non-existent");
        
        verify(paymentRepository, never()).save(any());
        verify(invoiceRepository, never()).save(any());
    }
    
    @Test
    void processPayment_shouldThrowException_whenInvoiceNotPending() {
        // Arrange - BR1: Only PENDING invoices can be paid
        Invoice paidInvoice = new Invoice();
        paidInvoice.setId("inv-2");
        paidInvoice.setStatus(InvoiceStatus.PAID);
        paidInvoice.setTotal(new BigDecimal("100.00"));
        
        when(invoiceRepository.findById("inv-2")).thenReturn(Optional.of(paidInvoice));
        
        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment("inv-2", validPaymentRequest, receivedBy))
            .isInstanceOf(InvalidInvoiceStatusException.class)
            .hasMessageContaining("Solo se pueden pagar facturas PENDIENTES");
        
        verify(paymentRepository, never()).save(any());
    }
    
    @Test
    void processPayment_shouldThrowException_whenInvoiceCancelled() {
        // Arrange - BR1: Only PENDING invoices can be paid
        Invoice cancelledInvoice = new Invoice();
        cancelledInvoice.setId("inv-3");
        cancelledInvoice.setStatus(InvoiceStatus.CANCELLED);
        cancelledInvoice.setTotal(new BigDecimal("100.00"));
        
        when(invoiceRepository.findById("inv-3")).thenReturn(Optional.of(cancelledInvoice));
        
        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment("inv-3", validPaymentRequest, receivedBy))
            .isInstanceOf(InvalidInvoiceStatusException.class)
            .hasMessageContaining("Solo se pueden pagar facturas PENDIENTES");
        
        verify(paymentRepository, never()).save(any());
    }
    
    @Test
    void processPayment_shouldThrowException_whenPaymentInsufficient() {
        // Arrange - BR3: Payment amount must be >= invoice total
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(pendingInvoice));
        
        ProcessPaymentRequest insufficientRequest = new ProcessPaymentRequest();
        insufficientRequest.setAmount(new BigDecimal("50.00")); // Less than total
        insufficientRequest.setMethod(PaymentMethod.CASH);
        
        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment("inv-1", insufficientRequest, receivedBy))
            .isInstanceOf(InsufficientPaymentException.class)
            .hasMessageContaining("El monto pagado (50.00) es insuficiente")
            .hasMessageContaining("Total de la factura: 100.00");
        
        verify(paymentRepository, never()).save(any());
        verify(invoiceRepository, times(1)).findById("inv-1"); // Only findById, no save
    }
    
    @Test
    void processPayment_shouldWorkWithCardPayment() {
        // Arrange
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(pendingInvoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId("payment-4");
            return payment;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ProcessPaymentRequest cardRequest = new ProcessPaymentRequest();
        cardRequest.setAmount(new BigDecimal("100.00"));
        cardRequest.setMethod(PaymentMethod.CARD);
        
        // Act
        PaymentResponse response = paymentService.processPayment("inv-1", cardRequest, receivedBy);
        
        // Assert
        assertThat(response.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(response.getChange()).isEqualByComparingTo("0.00");
    }
    
    @Test
    void processPayment_shouldWorkWithTransferPayment() {
        // Arrange
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(pendingInvoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId("payment-5");
            return payment;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ProcessPaymentRequest transferRequest = new ProcessPaymentRequest();
        transferRequest.setAmount(new BigDecimal("100.00"));
        transferRequest.setMethod(PaymentMethod.TRANSFER);
        
        // Act
        PaymentResponse response = paymentService.processPayment("inv-1", transferRequest, receivedBy);
        
        // Assert
        assertThat(response.getMethod()).isEqualTo(PaymentMethod.TRANSFER);
    }
    
    @Test
    void processPayment_shouldHandleInvoiceWithDiscount() {
        // Arrange
        Invoice discountedInvoice = new Invoice();
        discountedInvoice.setId("inv-4");
        discountedInvoice.setSubtotal(new BigDecimal("100.00"));
        discountedInvoice.setDiscountAmount(new BigDecimal("20.00"));
        discountedInvoice.setTotal(new BigDecimal("80.00")); // 100 - 20
        discountedInvoice.setStatus(InvoiceStatus.PENDING);
        
        when(invoiceRepository.findById("inv-4")).thenReturn(Optional.of(discountedInvoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId("payment-6");
            return payment;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setAmount(new BigDecimal("80.00"));
        request.setMethod(PaymentMethod.CASH);
        
        // Act
        PaymentResponse response = paymentService.processPayment("inv-4", request, receivedBy);
        
        // Assert
        assertThat(response.getAmount()).isEqualByComparingTo("80.00");
        assertThat(response.getChange()).isEqualByComparingTo("0.00");
    }
    
    @Test
    void processPayment_shouldSavePaymentWithCorrectFields() {
        // Arrange
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(pendingInvoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId("payment-7");
            return payment;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        
        // Act
        paymentService.processPayment("inv-1", validPaymentRequest, receivedBy);
        
        // Assert
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        
        assertThat(savedPayment.getInvoiceId()).isEqualTo("inv-1");
        assertThat(savedPayment.getAmount()).isEqualByComparingTo("100.00");
        assertThat(savedPayment.getChange()).isEqualByComparingTo("0.00");
        assertThat(savedPayment.getMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(savedPayment.getReceivedBy()).isEqualTo("cashier-123");
        assertThat(savedPayment.getPaidAt()).isNotNull();
    }
}
