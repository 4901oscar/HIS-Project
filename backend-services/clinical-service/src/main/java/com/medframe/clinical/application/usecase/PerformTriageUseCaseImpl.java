package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.model.PriorityLevel;
import com.medframe.clinical.domain.model.Triage;
import com.medframe.clinical.domain.port.in.PerformTriageUseCase;
import com.medframe.clinical.domain.port.out.AppointmentEmailSender;
import com.medframe.clinical.domain.port.out.TriageRepository;
import com.medframe.clinical.domain.service.TriageEngine;
import com.medframe.clinical.infrastructure.client.dto.TriageAlertRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PerformTriageUseCaseImpl implements PerformTriageUseCase {

    private final TriageEngine triageEngine;
    private final TriageRepository triageRepository;
    private final PermissionValidator permissionValidator;
    private final AppointmentEmailSender emailSender;

    public PerformTriageUseCaseImpl(TriageEngine triageEngine,
                                    TriageRepository triageRepository,
                                    PermissionValidator permissionValidator,
                                    AppointmentEmailSender emailSender) {
        this.triageEngine = triageEngine;
        this.triageRepository = triageRepository;
        this.permissionValidator = permissionValidator;
        this.emailSender = emailSender;
    }

    @Override
    public Triage performTriage(String appointmentId, String patientId, String doctorId,
                                String motifId, List<String> discriminatorIds) {
        permissionValidator.requireRole("VITAL_SIGNS");

        Triage triage = triageEngine.performTriage(appointmentId, patientId, doctorId,
                                                    motifId, discriminatorIds);
        Triage saved = triageRepository.save(triage);

        notifyDoctorIfUrgent(saved);

        return saved;
    }

    private void notifyDoctorIfUrgent(Triage triage) {
        PriorityLevel priority = triage.getPriorityLevel();
        if (priority != PriorityLevel.RED && priority != PriorityLevel.ORANGE) return;

        emailSender.sendTriageAlert(new TriageAlertRequest(
            triage.getDoctorId(),
            triage.getPatientId(),
            triage.getAppointmentId(),
            priority.name(),
            priority.getDescription(),
            priority.getMaxWaitMinutes()
        ));
    }
}
