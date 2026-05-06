package com.medframe.clinical.application.usecase;

import com.medframe.clinical.domain.model.Clinic;
import com.medframe.clinical.domain.model.ClinicStatus;
import com.medframe.clinical.domain.port.out.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case for listing clinics with optional filtering by status.
 * 
 * <p>This use case orchestrates clinic listing by:
 * <ol>
 *   <li>Retrieving clinics from the repository</li>
 *   <li>Optionally filtering by operational status</li>
 *   <li>Returning results sorted by createdAt descending</li>
 * </ol>
 * 
 * <p><b>Requirements:</b> 2.1, 2.2, 2.3, 2.4, 2.5
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ListClinicsUseCase {
    
    private final ClinicRepository clinicRepository;
    
    /**
     * Lists all clinics in the system.
     * 
     * <p>Results are sorted by createdAt timestamp in descending order
     * (most recently created first).
     * 
     * @return list of all clinics
     */
    public List<Clinic> execute() {
        return clinicRepository.findAll();
    }
    
    /**
     * Lists all clinics with a specific operational status.
     * 
     * <p>Results are sorted by createdAt timestamp in descending order
     * (most recently created first).
     * 
     * @param estado the clinic status to filter by (ACTIVE, INACTIVE, or DELETED)
     * @return list of clinics matching the specified status
     */
    public List<Clinic> executeWithFilter(ClinicStatus estado) {
        if (estado == null) {
            return execute();
        }
        
        return clinicRepository.findByEstado(estado);
    }
}
