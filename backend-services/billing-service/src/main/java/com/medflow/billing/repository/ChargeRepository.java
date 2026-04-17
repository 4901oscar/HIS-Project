package com.medflow.billing.repository;

import com.medflow.billing.model.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Charge entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods.
 * 
 * Custom query methods are automatically implemented by Spring Data JPA
 * based on method naming conventions.
 */
@Repository
public interface ChargeRepository extends JpaRepository<Charge, String> {
    
    /**
     * Find all charges for a specific invoice.
     * Spring Data JPA automatically navigates the invoice relationship
     * and queries by the invoice's id field.
     * 
     * @param invoiceId the ID of the invoice
     * @return list of charges belonging to the invoice
     */
    List<Charge> findByInvoiceId(String invoiceId);
}
