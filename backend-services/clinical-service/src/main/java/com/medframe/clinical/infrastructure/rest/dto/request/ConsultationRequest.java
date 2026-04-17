package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequest {
    
    @NotBlank(message = "Patient ID es requerido")
    private String patientId;
    
    private String appointmentId;
    
    @NotBlank(message = "Motivo de consulta es requerido")
    private String chiefComplaint;
    
    private String symptoms;
    
    @NotBlank(message = "Diagnóstico principal es requerido")
    @Pattern(regexp = "^[A-Z][0-9]{2}(\\.[0-9]{1,2})?$", 
             message = "Diagnóstico debe ser código CIE-10 válido")
    private String primaryDiagnosis;
    
    private List<@Pattern(regexp = "^[A-Z][0-9]{2}(\\.[0-9]{1,2})?$", 
                          message = "Diagnóstico debe ser código CIE-10 válido") String> secondaryDiagnoses;
    
    private String medicalNotes;
    
    private String treatmentPlan;
}
