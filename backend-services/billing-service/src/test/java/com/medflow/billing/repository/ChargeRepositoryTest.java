package com.medflow.billing.repository;

import com.medflow.billing.model.Charge;
import com.medflow.billing.model.ChargeType;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ChargeRepository.
 * Tests custom query method: findByInvoiceId.
 */
@DataJpaTest
class ChargeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChargeRepository chargeRepository;

    private Invoice invoice1;
    private Invoice invoice2;
    private Charge charge1;
    private Charge charge2;
    private Charge charge3;

    @BeforeEach
    void setUp() {
        // Create test invoices
        invoice1 = new Invoice();
        invoice1.setInvoiceNumber("INV-20260416-0001");
        invoice1.setPatientId("patient-123");
        invoice1.setSubtotal(new BigDecimal("300.00"));
        invoice1.setDiscountAmount(BigDecimal.ZERO);
        invoice1.setTotal(new BigDecimal("300.00"));
        invoice1.setStatus(InvoiceStatus.PENDING);
        invoice1.setCreatedAt(LocalDateTime.now());
        invoice1.setCreatedBy("cashier-1");

        invoice2 = new Invoice();
        invoice2.setInvoiceNumber("INV-20260416-0002");
        invoice2.setPatientId("patient-456");
        invoice2.setSubtotal(new BigDecimal("150.00"));
        invoice2.setDiscountAmount(BigDecimal.ZERO);
        invoice2.setTotal(new BigDecimal("150.00"));
        invoice2.setStatus(InvoiceStatus.PENDING);
        invoice2.setCreatedAt(LocalDateTime.now());
        invoice2.setCreatedBy("cashier-2");

        entityManager.persist(invoice1);
        entityManager.persist(invoice2);

        // Create test charges for invoice1
        charge1 = new Charge();
        charge1.setInvoice(invoice1);
        charge1.setType(ChargeType.CONSULTATION);
        charge1.setDescription("Consulta médica general");
        charge1.setQuantity(1);
        charge1.setUnitPrice(new BigDecimal("100.00"));
        charge1.setSubtotal(new BigDecimal("100.00"));

        charge2 = new Charge();
        charge2.setInvoice(invoice1);
        charge2.setType(ChargeType.LABORATORY);
        charge2.setDescription("Análisis de sangre");
        charge2.setQuantity(2);
        charge2.setUnitPrice(new BigDecimal("100.00"));
        charge2.setSubtotal(new BigDecimal("200.00"));

        // Create test charge for invoice2
        charge3 = new Charge();
        charge3.setInvoice(invoice2);
        charge3.setType(ChargeType.MEDICATION);
        charge3.setDescription("Antibiótico");
        charge3.setQuantity(3);
        charge3.setUnitPrice(new BigDecimal("50.00"));
        charge3.setSubtotal(new BigDecimal("150.00"));

        entityManager.persist(charge1);
        entityManager.persist(charge2);
        entityManager.persist(charge3);
        entityManager.flush();
    }

    @Test
    void findByInvoiceId_shouldReturnAllChargesForInvoice() {
        // When
        List<Charge> charges = chargeRepository.findByInvoiceId(invoice1.getId());

        // Then
        assertThat(charges).hasSize(2);
        assertThat(charges).extracting(Charge::getDescription)
                .containsExactlyInAnyOrder("Consulta médica general", "Análisis de sangre");
        assertThat(charges).extracting(Charge::getType)
                .containsExactlyInAnyOrder(ChargeType.CONSULTATION, ChargeType.LABORATORY);
    }

    @Test
    void findByInvoiceId_shouldReturnEmptyListWhenNoChargesFound() {
        // Given - create an invoice with no charges
        Invoice emptyInvoice = new Invoice();
        emptyInvoice.setInvoiceNumber("INV-20260416-0003");
        emptyInvoice.setPatientId("patient-789");
        emptyInvoice.setSubtotal(BigDecimal.ZERO);
        emptyInvoice.setDiscountAmount(BigDecimal.ZERO);
        emptyInvoice.setTotal(BigDecimal.ZERO);
        emptyInvoice.setStatus(InvoiceStatus.PENDING);
        emptyInvoice.setCreatedAt(LocalDateTime.now());
        emptyInvoice.setCreatedBy("cashier-3");
        entityManager.persist(emptyInvoice);
        entityManager.flush();

        // When
        List<Charge> charges = chargeRepository.findByInvoiceId(emptyInvoice.getId());

        // Then
        assertThat(charges).isEmpty();
    }

    @Test
    void findByInvoiceId_shouldReturnEmptyListForNonExistentInvoice() {
        // When
        List<Charge> charges = chargeRepository.findByInvoiceId("non-existent-id");

        // Then
        assertThat(charges).isEmpty();
    }

    @Test
    void findByInvoiceId_shouldReturnCorrectChargeDetails() {
        // When
        List<Charge> charges = chargeRepository.findByInvoiceId(invoice2.getId());

        // Then
        assertThat(charges).hasSize(1);
        Charge charge = charges.get(0);
        assertThat(charge.getType()).isEqualTo(ChargeType.MEDICATION);
        assertThat(charge.getDescription()).isEqualTo("Antibiótico");
        assertThat(charge.getQuantity()).isEqualTo(3);
        assertThat(charge.getUnitPrice()).isEqualByComparingTo("50.00");
        assertThat(charge.getSubtotal()).isEqualByComparingTo("150.00");
    }
}
