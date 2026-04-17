package com.medflow.billing.repository;

import com.medflow.billing.model.Payment;
import com.medflow.billing.model.PaymentMethod;
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
 * Unit tests for PaymentRepository.
 * Tests custom query method: findByInvoiceId.
 */
@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment1;
    private Payment payment2;
    private Payment payment3;

    @BeforeEach
    void setUp() {
        // Create test payments
        payment1 = new Payment();
        payment1.setInvoiceId("invoice-123");
        payment1.setAmount(new BigDecimal("100.00"));
        payment1.setChange(BigDecimal.ZERO);
        payment1.setMethod(PaymentMethod.CASH);
        payment1.setPaidAt(LocalDateTime.now());
        payment1.setReceivedBy("cashier-1");

        payment2 = new Payment();
        payment2.setInvoiceId("invoice-123");
        payment2.setAmount(new BigDecimal("50.00"));
        payment2.setChange(BigDecimal.ZERO);
        payment2.setMethod(PaymentMethod.CARD);
        payment2.setPaidAt(LocalDateTime.now());
        payment2.setReceivedBy("cashier-1");

        payment3 = new Payment();
        payment3.setInvoiceId("invoice-456");
        payment3.setAmount(new BigDecimal("200.00"));
        payment3.setChange(new BigDecimal("10.00"));
        payment3.setMethod(PaymentMethod.CASH);
        payment3.setPaidAt(LocalDateTime.now());
        payment3.setReceivedBy("cashier-2");

        entityManager.persist(payment1);
        entityManager.persist(payment2);
        entityManager.persist(payment3);
        entityManager.flush();
    }

    @Test
    void findByInvoiceId_shouldReturnAllPaymentsForInvoice() {
        // When
        List<Payment> payments = paymentRepository.findByInvoiceId("invoice-123");

        // Then
        assertThat(payments).hasSize(2);
        assertThat(payments).extracting(Payment::getInvoiceId)
                .containsOnly("invoice-123");
        assertThat(payments).extracting(Payment::getAmount)
                .containsExactlyInAnyOrder(new BigDecimal("100.00"), new BigDecimal("50.00"));
    }

    @Test
    void findByInvoiceId_shouldReturnEmptyListWhenNoPaymentsFound() {
        // When
        List<Payment> payments = paymentRepository.findByInvoiceId("invoice-999");

        // Then
        assertThat(payments).isEmpty();
    }

    @Test
    void findByInvoiceId_shouldReturnSinglePaymentWhenOnlyOneExists() {
        // When
        List<Payment> payments = paymentRepository.findByInvoiceId("invoice-456");

        // Then
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getInvoiceId()).isEqualTo("invoice-456");
        assertThat(payments.get(0).getAmount()).isEqualByComparingTo("200.00");
        assertThat(payments.get(0).getChange()).isEqualByComparingTo("10.00");
        assertThat(payments.get(0).getMethod()).isEqualTo(PaymentMethod.CASH);
    }

    @Test
    void findByInvoiceId_shouldReturnPaymentsWithCorrectDetails() {
        // When
        List<Payment> payments = paymentRepository.findByInvoiceId("invoice-123");

        // Then
        assertThat(payments).hasSize(2);
        assertThat(payments).allSatisfy(payment -> {
            assertThat(payment.getInvoiceId()).isEqualTo("invoice-123");
            assertThat(payment.getReceivedBy()).isEqualTo("cashier-1");
            assertThat(payment.getPaidAt()).isNotNull();
        });
    }
}
