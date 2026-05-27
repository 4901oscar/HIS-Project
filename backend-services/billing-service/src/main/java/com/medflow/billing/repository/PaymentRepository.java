package com.medflow.billing.repository;

import com.medflow.billing.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Payment entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods.
 * 
 * Custom query methods are automatically implemented by Spring Data JPA
 * based on method naming conventions.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    /**
     * Find all payments for a specific invoice.
     * 
     * @param invoiceId the ID of the invoice
     * @return list of payments associated with the invoice
     */
    List<Payment> findByInvoiceId(String invoiceId);
}
