package com.medflow.billing.service;

import com.medflow.billing.dto.request.ApplyDiscountRequest;
import com.medflow.billing.dto.request.ChargeRequest;
import com.medflow.billing.dto.request.CreateInvoiceRequest;
import com.medflow.billing.dto.response.InvoiceResponse;
import com.medflow.billing.exception.InvoiceNotFoundException;
import com.medflow.billing.exception.InvalidInvoiceStatusException;
import com.medflow.billing.model.Charge;
import com.medflow.billing.model.ChargeType;
import com.medflow.billing.model.Invoice;
import com.medflow.billing.model.InvoiceStatus;
import com.medflow.billing.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InvoiceService.
 * Tests invoice creation, calculation logic, and invoice number generation.
 */
@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    
    @Mock
    private InvoiceRepository invoiceRepository;
    
    @InjectMocks
    private InvoiceService invoiceService;
    
    private CreateInvoiceRequest validRequest;
    private String createdBy;
    
    @BeforeEach
    void setUp() {
        createdBy = "user-123";
        
        // Create a valid request with multiple charges
        ChargeRequest charge1 = new ChargeRequest(
            ChargeType.CONSULTATION,
            "Consulta General",
            1,
            new BigDecimal("50.00")
        );
        
        ChargeRequest charge2 = new ChargeRequest(
            ChargeType.LABORATORY,
            "Análisis de Sangre",
            2,
            new BigDecimal("30.00")
        );
        
        validRequest = new CreateInvoiceRequest(
            "patient-456",
            Arrays.asList(charge1, charge2)
        );
    }
    
    @Test
    void createInvoice_shouldGenerateInvoiceNumber() {
        // Arrange
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId("invoice-id-123");
            return invoice;
        });
        
        // Act
        InvoiceResponse response = invoiceService.createInvoice(validRequest, createdBy);
        
        // Assert
        assertThat(response.getInvoiceNumber()).matches("INV-\\d{8}-\\d{4}");
    }
    
    @Test
    void createInvoice_shouldCalculateChargeSubtotals() {
        // Arrange
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId("invoice-id-123");
            return invoice;
        });
        
        // Act
        InvoiceResponse response = invoiceService.createInvoice(validRequest, createdBy);
        
        // Assert
        assertThat(response.getCharges()).hasSize(2);
        assertThat(response.getCharges().get(0).getSubtotal()).isEqualByComparingTo("50.00"); // 1 * 50
        assertThat(response.getCharges().get(1).getSubtotal()).isEqualByComparingTo("60.00"); // 2 * 30
    }
    
    @Test
    void createInvoice_shouldCalculateTotalSubtotal() {
        // Arrange
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId("invoice-id-123");
            return invoice;
        });
        
        // Act
        InvoiceResponse response = invoiceService.createInvoice(validRequest, createdBy);
        
        // Assert
        assertThat(response.getSubtotal()).isEqualByComparingTo("110.00"); // 50 + 60
        assertThat(response.getTotal()).isEqualByComparingTo("110.00"); // No discount yet
    }
    
    @Test
    void createInvoice_shouldSetInitialStatusToPending() {
        // Arrange
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId("invoice-id-123");
            return invoice;
        });
        
        // Act
        InvoiceResponse response = invoiceService.createInvoice(validRequest, createdBy);
        
        // Assert
        assertThat(response.getStatus()).isEqualTo(InvoiceStatus.PENDING);
    }
    
    @Test
    void createInvoice_shouldSetPatientIdAndCreatedBy() {
        // Arrange
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId("invoice-id-123");
            return invoice;
        });
        
        // Act
        InvoiceResponse response = invoiceService.createInvoice(validRequest, createdBy);
        
        // Assert
        assertThat(response.getPatientId()).isEqualTo("patient-456");
        assertThat(response.getCreatedBy()).isEqualTo("user-123");
        assertThat(response.getCreatedAt()).isNotNull();
    }
    
    @Test
    void createInvoice_shouldSetDiscountAmountToZero() {
        // Arrange
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId("invoice-id-123");
            return invoice;
        });
        
        // Act
        InvoiceResponse response = invoiceService.createInvoice(validRequest, createdBy);
        
        // Assert
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("0.00");
    }
    
    @Test
    void createInvoice_shouldSaveInvoiceWithCharges() {
        // Arrange
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId("invoice-id-123");
            return invoice;
        });
        
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        
        // Act
        invoiceService.createInvoice(validRequest, createdBy);
        
        // Assert
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice savedInvoice = invoiceCaptor.getValue();
        
        assertThat(savedInvoice.getCharges()).hasSize(2);
        assertThat(savedInvoice.getCharges().get(0).getInvoice()).isEqualTo(savedInvoice);
        assertThat(savedInvoice.getCharges().get(1).getInvoice()).isEqualTo(savedInvoice);
    }
    
    @Test
    void createInvoice_withSingleCharge_shouldCalculateCorrectly() {
        // Arrange
        ChargeRequest singleCharge = new ChargeRequest(
            ChargeType.MEDICATION,
            "Paracetamol",
            5,
            new BigDecimal("10.00")
        );
        
        CreateInvoiceRequest singleChargeRequest = new CreateInvoiceRequest(
            "patient-789",
            List.of(singleCharge)
        );
        
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId("invoice-id-456");
            return invoice;
        });
        
        // Act
        InvoiceResponse response = invoiceService.createInvoice(singleChargeRequest, createdBy);
        
        // Assert
        assertThat(response.getCharges()).hasSize(1);
        assertThat(response.getCharges().get(0).getSubtotal()).isEqualByComparingTo("50.00"); // 5 * 10
        assertThat(response.getSubtotal()).isEqualByComparingTo("50.00");
        assertThat(response.getTotal()).isEqualByComparingTo("50.00");
    }
    
    @Test
    void createInvoice_withDecimalPrices_shouldCalculateCorrectly() {
        // Arrange
        ChargeRequest charge = new ChargeRequest(
            ChargeType.OTHER,
            "Servicio Especial",
            3,
            new BigDecimal("15.75")
        );
        
        CreateInvoiceRequest request = new CreateInvoiceRequest(
            "patient-999",
            List.of(charge)
        );
        
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId("invoice-id-789");
            return invoice;
        });
        
        // Act
        InvoiceResponse response = invoiceService.createInvoice(request, createdBy);
        
        // Assert
        assertThat(response.getCharges().get(0).getSubtotal()).isEqualByComparingTo("47.25"); // 3 * 15.75
        assertThat(response.getTotal()).isEqualByComparingTo("47.25");
    }
    
    @Test
    void getInvoices_withoutStatusFilter_shouldReturnAllInvoices() {
        // Arrange
        List<Invoice> mockInvoices = createMockInvoices();
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        
        // Act
        List<InvoiceResponse> responses = invoiceService.getInvoices(null);
        
        // Assert
        assertThat(responses).hasSize(3);
        verify(invoiceRepository).findAll();
        verify(invoiceRepository, never()).findByStatus(any());
    }
    
    @Test
    void getInvoices_withStatusFilter_shouldReturnFilteredInvoices() {
        // Arrange
        List<Invoice> pendingInvoices = createMockInvoices().stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.PENDING)
            .toList();
        when(invoiceRepository.findByStatus(InvoiceStatus.PENDING)).thenReturn(pendingInvoices);
        
        // Act
        List<InvoiceResponse> responses = invoiceService.getInvoices(InvoiceStatus.PENDING);
        
        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses).allMatch(r -> r.getStatus() == InvoiceStatus.PENDING);
        verify(invoiceRepository).findByStatus(InvoiceStatus.PENDING);
        verify(invoiceRepository, never()).findAll();
    }
    
    @Test
    void getInvoices_withPaidStatus_shouldReturnOnlyPaidInvoices() {
        // Arrange
        List<Invoice> paidInvoices = createMockInvoices().stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
            .toList();
        when(invoiceRepository.findByStatus(InvoiceStatus.PAID)).thenReturn(paidInvoices);
        
        // Act
        List<InvoiceResponse> responses = invoiceService.getInvoices(InvoiceStatus.PAID);
        
        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(InvoiceStatus.PAID);
        verify(invoiceRepository).findByStatus(InvoiceStatus.PAID);
    }
    
    @Test
    void getInvoices_withCancelledStatus_shouldReturnEmptyList() {
        // Arrange
        when(invoiceRepository.findByStatus(InvoiceStatus.CANCELLED)).thenReturn(List.of());
        
        // Act
        List<InvoiceResponse> responses = invoiceService.getInvoices(InvoiceStatus.CANCELLED);
        
        // Assert
        assertThat(responses).isEmpty();
        verify(invoiceRepository).findByStatus(InvoiceStatus.CANCELLED);
    }
    
    @Test
    void getInvoices_shouldMapInvoiceFieldsCorrectly() {
        // Arrange
        List<Invoice> mockInvoices = createMockInvoices();
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        
        // Act
        List<InvoiceResponse> responses = invoiceService.getInvoices(null);
        
        // Assert
        InvoiceResponse firstResponse = responses.get(0);
        Invoice firstInvoice = mockInvoices.get(0);
        
        assertThat(firstResponse.getId()).isEqualTo(firstInvoice.getId());
        assertThat(firstResponse.getInvoiceNumber()).isEqualTo(firstInvoice.getInvoiceNumber());
        assertThat(firstResponse.getPatientId()).isEqualTo(firstInvoice.getPatientId());
        assertThat(firstResponse.getSubtotal()).isEqualByComparingTo(firstInvoice.getSubtotal());
        assertThat(firstResponse.getTotal()).isEqualByComparingTo(firstInvoice.getTotal());
        assertThat(firstResponse.getStatus()).isEqualTo(firstInvoice.getStatus());
    }
    
    /**
     * Helper method to create mock invoices for testing.
     */
    private List<Invoice> createMockInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        
        // Invoice 1 - PENDING
        Invoice invoice1 = new Invoice();
        invoice1.setId("inv-1");
        invoice1.setInvoiceNumber("INV-20260416-0001");
        invoice1.setPatientId("patient-1");
        invoice1.setStatus(InvoiceStatus.PENDING);
        invoice1.setSubtotal(new BigDecimal("100.00"));
        invoice1.setDiscountAmount(BigDecimal.ZERO);
        invoice1.setTotal(new BigDecimal("100.00"));
        invoice1.setCreatedAt(LocalDateTime.now());
        invoice1.setCreatedBy("user-1");
        invoice1.setCharges(createMockCharges(invoice1));
        invoices.add(invoice1);
        
        // Invoice 2 - PENDING
        Invoice invoice2 = new Invoice();
        invoice2.setId("inv-2");
        invoice2.setInvoiceNumber("INV-20260416-0002");
        invoice2.setPatientId("patient-2");
        invoice2.setStatus(InvoiceStatus.PENDING);
        invoice2.setSubtotal(new BigDecimal("200.00"));
        invoice2.setDiscountAmount(BigDecimal.ZERO);
        invoice2.setTotal(new BigDecimal("200.00"));
        invoice2.setCreatedAt(LocalDateTime.now());
        invoice2.setCreatedBy("user-2");
        invoice2.setCharges(createMockCharges(invoice2));
        invoices.add(invoice2);
        
        // Invoice 3 - PAID
        Invoice invoice3 = new Invoice();
        invoice3.setId("inv-3");
        invoice3.setInvoiceNumber("INV-20260416-0003");
        invoice3.setPatientId("patient-3");
        invoice3.setStatus(InvoiceStatus.PAID);
        invoice3.setSubtotal(new BigDecimal("150.00"));
        invoice3.setDiscountAmount(BigDecimal.ZERO);
        invoice3.setTotal(new BigDecimal("150.00"));
        invoice3.setCreatedAt(LocalDateTime.now());
        invoice3.setCreatedBy("user-3");
        invoice3.setCharges(createMockCharges(invoice3));
        invoices.add(invoice3);
        
        return invoices;
    }
    
    /**
     * Helper method to create mock charges for an invoice.
     */
    private List<Charge> createMockCharges(Invoice invoice) {
        Charge charge = new Charge();
        charge.setId("charge-" + invoice.getId());
        charge.setInvoice(invoice);
        charge.setType(ChargeType.CONSULTATION);
        charge.setDescription("Consulta General");
        charge.setQuantity(1);
        charge.setUnitPrice(new BigDecimal("50.00"));
        charge.setSubtotal(new BigDecimal("50.00"));
        return List.of(charge);
    }
    
    // ========== Tests for getById() ==========
    
    @Test
    void getById_shouldReturnInvoice_whenInvoiceExists() {
        // Arrange
        Invoice mockInvoice = createMockInvoices().get(0);
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(mockInvoice));
        
        // Act
        InvoiceResponse response = invoiceService.getById("inv-1");
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("inv-1");
        assertThat(response.getInvoiceNumber()).isEqualTo("INV-20260416-0001");
        verify(invoiceRepository).findById("inv-1");
    }
    
    @Test
    void getById_shouldThrowException_whenInvoiceNotFound() {
        // Arrange
        when(invoiceRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> invoiceService.getById("non-existent"))
            .isInstanceOf(InvoiceNotFoundException.class)
            .hasMessageContaining("Factura no encontrada con ID: non-existent");
        
        verify(invoiceRepository).findById("non-existent");
    }
    
    // ========== Tests for applyDiscount() ==========
    
    @Test
    void applyDiscount_withFixedAmount_shouldApplyDiscountCorrectly() {
        // Arrange
        Invoice mockInvoice = createMockInvoices().get(0); // subtotal = 100.00
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(mockInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ApplyDiscountRequest request = new ApplyDiscountRequest();
        request.setDiscountAmount(new BigDecimal("20.00"));
        
        // Act
        InvoiceResponse response = invoiceService.applyDiscount("inv-1", request, null);
        
        // Assert
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("20.00");
        assertThat(response.getTotal()).isEqualByComparingTo("80.00"); // 100 - 20
        verify(invoiceRepository).save(any(Invoice.class));
    }
    
    @Test
    void applyDiscount_withPercentage_shouldCalculateDiscountCorrectly() {
        // Arrange
        Invoice mockInvoice = createMockInvoices().get(0); // subtotal = 100.00
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(mockInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ApplyDiscountRequest request = new ApplyDiscountRequest();
        request.setDiscountPercentage(new BigDecimal("15.00")); // 15%
        
        // Act
        InvoiceResponse response = invoiceService.applyDiscount("inv-1", request, null);
        
        // Assert
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("15.00"); // 15% of 100
        assertThat(response.getTotal()).isEqualByComparingTo("85.00"); // 100 - 15
        verify(invoiceRepository).save(any(Invoice.class));
    }
    
    @Test
    void applyDiscount_shouldCapDiscountAtSubtotal_whenDiscountExceedsSubtotal() {
        // Arrange
        Invoice mockInvoice = createMockInvoices().get(0); // subtotal = 100.00
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(mockInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ApplyDiscountRequest request = new ApplyDiscountRequest();
        request.setDiscountAmount(new BigDecimal("150.00")); // More than subtotal
        
        // Act
        InvoiceResponse response = invoiceService.applyDiscount("inv-1", request, null);
        
        // Assert - BR7: Total never negative
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("100.00"); // Capped at subtotal
        assertThat(response.getTotal()).isEqualByComparingTo("0.00"); // 100 - 100
        verify(invoiceRepository).save(any(Invoice.class));
    }
    
    @Test
    void applyDiscount_shouldThrowException_whenInvoiceNotFound() {
        // Arrange
        when(invoiceRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        ApplyDiscountRequest request = new ApplyDiscountRequest();
        request.setDiscountAmount(new BigDecimal("10.00"));
        
        // Act & Assert
        assertThatThrownBy(() -> invoiceService.applyDiscount("non-existent", request, null))
            .isInstanceOf(InvoiceNotFoundException.class)
            .hasMessageContaining("Factura no encontrada con ID: non-existent");
    }
    
    @Test
    void applyDiscount_shouldThrowException_whenInvoiceNotPending() {
        // Arrange
        Invoice paidInvoice = createMockInvoices().get(2); // Status = PAID
        when(invoiceRepository.findById("inv-3")).thenReturn(Optional.of(paidInvoice));
        
        ApplyDiscountRequest request = new ApplyDiscountRequest();
        request.setDiscountAmount(new BigDecimal("10.00"));
        
        // Act & Assert - BR2: Only PENDING invoices can receive discounts
        assertThatThrownBy(() -> invoiceService.applyDiscount("inv-3", request, null))
            .isInstanceOf(InvalidInvoiceStatusException.class)
            .hasMessageContaining("Solo se pueden aplicar descuentos a facturas PENDIENTES");
    }
    
    @Test
    void applyDiscount_shouldThrowException_whenNoDiscountProvided() {
        // Arrange
        Invoice mockInvoice = createMockInvoices().get(0);
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(mockInvoice));
        
        ApplyDiscountRequest request = new ApplyDiscountRequest(); // No discount set
        
        // Act & Assert
        assertThatThrownBy(() -> invoiceService.applyDiscount("inv-1", request, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Debe proporcionar un monto o porcentaje de descuento");
    }
    
    // ========== Tests for cancelInvoice() ==========
    
    @Test
    void cancelInvoice_shouldUpdateStatusToCancelled_whenInvoiceIsPending() {
        // Arrange
        Invoice mockInvoice = createMockInvoices().get(0); // Status = PENDING
        when(invoiceRepository.findById("inv-1")).thenReturn(Optional.of(mockInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        InvoiceResponse response = invoiceService.cancelInvoice("inv-1", null);
        
        // Assert
        assertThat(response.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        verify(invoiceRepository).save(any(Invoice.class));
    }
    
    @Test
    void cancelInvoice_shouldThrowException_whenInvoiceNotFound() {
        // Arrange
        when(invoiceRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> invoiceService.cancelInvoice("non-existent", null))
            .isInstanceOf(InvoiceNotFoundException.class)
            .hasMessageContaining("Factura no encontrada con ID: non-existent");
    }
    
    @Test
    void cancelInvoice_shouldThrowException_whenInvoiceNotPending() {
        // Arrange
        Invoice paidInvoice = createMockInvoices().get(2); // Status = PAID
        when(invoiceRepository.findById("inv-3")).thenReturn(Optional.of(paidInvoice));
        
        // Act & Assert - BR2: Only PENDING invoices can be cancelled
        assertThatThrownBy(() -> invoiceService.cancelInvoice("inv-3", null))
            .isInstanceOf(InvalidInvoiceStatusException.class)
            .hasMessageContaining("Solo se pueden cancelar facturas PENDIENTES");
    }
    
    // ========== Tests for getPatientInvoices() ==========
    
    @Test
    void getPatientInvoices_shouldReturnInvoices_whenCashierAccesses() {
        // Arrange
        List<Invoice> patientInvoices = List.of(createMockInvoices().get(0));
        when(invoiceRepository.findByPatientId("patient-1")).thenReturn(patientInvoices);
        
        // Act
        List<InvoiceResponse> responses = invoiceService.getPatientInvoices(
            "patient-1", "cashier-123", "CASHIER");
        
        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getPatientId()).isEqualTo("patient-1");
        verify(invoiceRepository).findByPatientId("patient-1");
    }
    
    @Test
    void getPatientInvoices_shouldReturnInvoices_whenPatientAccessesOwnInvoices() {
        // Arrange
        List<Invoice> patientInvoices = List.of(createMockInvoices().get(0));
        when(invoiceRepository.findByPatientId("patient-1")).thenReturn(patientInvoices);
        
        // Act - BR4: Patient accessing their own invoices
        List<InvoiceResponse> responses = invoiceService.getPatientInvoices(
            "patient-1", "patient-1", "PATIENT");
        
        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getPatientId()).isEqualTo("patient-1");
        verify(invoiceRepository).findByPatientId("patient-1");
    }
    
    @Test
    void getPatientInvoices_shouldThrowException_whenPatientAccessesOtherPatientInvoices() {
        // Arrange - Patient trying to access another patient's invoices
        
        // Act & Assert - BR4: Patient can only access their own invoices
        assertThatThrownBy(() -> invoiceService.getPatientInvoices(
            "patient-1", "patient-2", "PATIENT"))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("No tiene permiso para ver las facturas de otro paciente");
        
        verify(invoiceRepository, never()).findByPatientId(any());
    }
    
    @Test
    void getPatientInvoices_shouldReturnInvoices_whenAdminAccesses() {
        // Arrange
        List<Invoice> patientInvoices = List.of(createMockInvoices().get(0));
        when(invoiceRepository.findByPatientId("patient-1")).thenReturn(patientInvoices);
        
        // Act
        List<InvoiceResponse> responses = invoiceService.getPatientInvoices(
            "patient-1", "admin-123", "ADMIN");
        
        // Assert
        assertThat(responses).hasSize(1);
        verify(invoiceRepository).findByPatientId("patient-1");
    }
}
