package com.medframe.clinical.application.usecase;

import com.medframe.clinical.domain.model.Triage;
import com.medframe.clinical.domain.port.in.GetAppointmentTriageUseCase;
import com.medframe.clinical.domain.port.out.TriageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of GetAppointmentTriageUseCase.
 * 
 * This use case retrieves the triage record associated with a specific appointment.
 * It provides a simple query interface for retrieving triage data by appointment ID.
 * 
 * Requirements: 3.1, 3.2, 3.3 (Query Triage by Appointment)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class GetAppointmentTriageUseCaseImpl implements GetAppointmentTriageUseCase {
    
    private final TriageRepository triageRepository;
    
    public GetAppointmentTriageUseCaseImpl(TriageRepository triageRepository) {
        this.triageRepository = triageRepository;
    }
    
    /**
     * Retrieves the triage record associated with a specific appointment.
     * 
     * @param appointmentId The appointment's unique identifier
     * @return Optional containing the Triage if found, empty otherwise
     */
    @Override
    public Optional<Triage> getAppointmentTriage(String appointmentId) {
        return triageRepository.findByAppointmentId(appointmentId);
    }
}
