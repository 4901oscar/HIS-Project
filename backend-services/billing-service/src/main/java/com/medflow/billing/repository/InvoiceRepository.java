package com.medflow.billing.repository;

import com.medflow.billing.model.Invoice;
import com.medflow.billing.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Invoice entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods.
 * 
 * Custom query methods are automatically implemented by Spring Data JPA
 * based on method naming conventions.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    
    /**
     * Find all invoices for a specific patient.
     * 
     * @param patientId the ID of the patient
     * @return list of invoices belonging to the patient
     */
    List<Invoice> findByPatientId(String patientId);
    
    /**
     * Find all invoices with a specific status.
     * 
     * @param status the invoice status (PENDING, PAID, CANCELLED)
     * @return list of invoices with the specified status
     */
    List<Invoice> findByStatus(InvoiceStatus status);
    
    /**
     * Find an invoice by its unique invoice number.
     * 
     * @param invoiceNumber the invoice number (format: INV-YYYYMMDD-XXXX)
     * @return Optional containing the invoice if found, empty otherwise
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
