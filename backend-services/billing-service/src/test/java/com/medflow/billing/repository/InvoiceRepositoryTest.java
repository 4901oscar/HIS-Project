package com.medflow.billing.repository;

import com.medflow.billing.model.Invoice;
import com.medflow.billing.model.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for InvoiceRepository.
 * Tests custom query methods: findByPatientId, findByStatus, findByInvoiceNumber.
 */
@DataJpaTest
class InvoiceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Invoice invoice1;
    private Invoice invoice2;
    private Invoice invoice3;

    @BeforeEach
    void setUp() {
        // Create test invoices
        invoice1 = new Invoice();
        invoice1.setInvoiceNumber("INV-20260416-0001");
        invoice1.setPatientId("patient-123");
        invoice1.setSubtotal(new BigDecimal("100.00"));
        invoice1.setDiscountAmount(BigDecimal.ZERO);
        invoice1.setTotal(new BigDecimal("100.00"));
        invoice1.setStatus(InvoiceStatus.PENDING);
        invoice1.setCreatedAt(LocalDateTime.now());
        invoice1.setCreatedBy("cashier-1");

        invoice2 = new Invoice();
        invoice2.setInvoiceNumber("INV-20260416-0002");
        invoice2.setPatientId("patient-123");
        invoice2.setSubtotal(new BigDecimal("200.00"));
        invoice2.setDiscountAmount(BigDecimal.ZERO);
        invoice2.setTotal(new BigDecimal("200.00"));
        invoice2.setStatus(InvoiceStatus.PAID);
        invoice2.setCreatedAt(LocalDateTime.now());
        invoice2.setCreatedBy("cashier-1");

        invoice3 = new Invoice();
        invoice3.setInvoiceNumber("INV-20260416-0003");
        invoice3.setPatientId("patient-456");
        invoice3.setSubtotal(new BigDecimal("150.00"));
        invoice3.setDiscountAmount(BigDecimal.ZERO);
        invoice3.setTotal(new BigDecimal("150.00"));
        invoice3.setStatus(InvoiceStatus.PENDING);
        invoice3.setCreatedAt(LocalDateTime.now());
        invoice3.setCreatedBy("cashier-2");

        entityManager.persist(invoice1);
        entityManager.persist(invoice2);
        entityManager.persist(invoice3);
        entityManager.flush();
    }

    @Test
    void findByPatientId_shouldReturnAllInvoicesForPatient() {
        // When
        List<Invoice> invoices = invoiceRepository.findByPatientId("patient-123");

        // Then
        assertThat(invoices).hasSize(2);
        assertThat(invoices).extracting(Invoice::getPatientId)
                .containsOnly("patient-123");
        assertThat(invoices).extracting(Invoice::getInvoiceNumber)
                .containsExactlyInAnyOrder("INV-20260416-0001", "INV-20260416-0002");
    }

    @Test
    void findByPatientId_shouldReturnEmptyListWhenNoInvoicesFound() {
        // When
        List<Invoice> invoices = invoiceRepository.findByPatientId("patient-999");

        // Then
        assertThat(invoices).isEmpty();
    }

    @Test
    void findByStatus_shouldReturnAllPendingInvoices() {
        // When
        List<Invoice> invoices = invoiceRepository.findByStatus(InvoiceStatus.PENDING);

        // Then
        assertThat(invoices).hasSize(2);
        assertThat(invoices).extracting(Invoice::getStatus)
                .containsOnly(InvoiceStatus.PENDING);
        assertThat(invoices).extracting(Invoice::getInvoiceNumber)
                .containsExactlyInAnyOrder("INV-20260416-0001", "INV-20260416-0003");
    }

    @Test
    void findByStatus_shouldReturnAllPaidInvoices() {
        // When
        List<Invoice> invoices = invoiceRepository.findByStatus(InvoiceStatus.PAID);

        // Then
        assertThat(invoices).hasSize(1);
        assertThat(invoices.get(0).getInvoiceNumber()).isEqualTo("INV-20260416-0002");
        assertThat(invoices.get(0).getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void findByStatus_shouldReturnEmptyListWhenNoInvoicesWithStatus() {
        // When
        List<Invoice> invoices = invoiceRepository.findByStatus(InvoiceStatus.CANCELLED);

        // Then
        assertThat(invoices).isEmpty();
    }

    @Test
    void findByInvoiceNumber_shouldReturnInvoiceWhenExists() {
        // When
        Optional<Invoice> result = invoiceRepository.findByInvoiceNumber("INV-20260416-0001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getInvoiceNumber()).isEqualTo("INV-20260416-0001");
        assertThat(result.get().getPatientId()).isEqualTo("patient-123");
        assertThat(result.get().getTotal()).isEqualByComparingTo("100.00");
    }

    @Test
    void findByInvoiceNumber_shouldReturnEmptyWhenNotExists() {
        // When
        Optional<Invoice> result = invoiceRepository.findByInvoiceNumber("INV-20260416-9999");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByInvoiceNumber_shouldBeUnique() {
        // Given - invoice1 already has "INV-20260416-0001"
        
        // When
        Optional<Invoice> result = invoiceRepository.findByInvoiceNumber("INV-20260416-0001");

        // Then
        assertThat(result).isPresent();
        assertThat(invoiceRepository.findByInvoiceNumber("INV-20260416-0001"))
                .hasValueSatisfying(invoice -> 
                    assertThat(invoice.getId()).isEqualTo(invoice1.getId())
                );
    }
}
