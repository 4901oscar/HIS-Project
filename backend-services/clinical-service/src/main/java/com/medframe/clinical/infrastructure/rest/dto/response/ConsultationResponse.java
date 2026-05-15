package com.medframe.clinical.infrastructure.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationResponse {

    private String id;
    private String patientId;
    private String doctorId;
    private String chiefComplaint;
    private String symptoms;
    private String primaryDiagnosis;
    private List<String> secondaryDiagnoses;
    private String medicalNotes;
    private String treatmentPlan;
    private LocalDateTime consultationDate;
}
