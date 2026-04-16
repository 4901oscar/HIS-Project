package com.medflow.patient.service;

import com.medflow.patient.dto.CreatePatientRequest;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.patient.dto.UpdatePatientRequest;
import com.medflow.patient.exception.DuplicateDpiException;
import com.medflow.patient.exception.DuplicateEmailException;
import com.medflow.patient.exception.PatientNotFoundException;
import com.medflow.patient.model.Patient;
import com.medflow.patient.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final AuthServiceClient authServiceClient;

    public PatientService(PatientRepository patientRepository, AuthServiceClient authServiceClient) {
        this.patientRepository = patientRepository;
        this.authServiceClient = authServiceClient;
    }

    /**
     * CU-01: Admisión registra paciente presencialmente.
     * Crea datos demográficos + cuenta en auth-service con contraseña temporal.
     */
    @Transactional
    public PatientResponse createPatient(CreatePatientRequest request) {
        if (patientRepository.existsByDpi(request.getDpi())) {
            throw new DuplicateDpiException(request.getDpi());
        }
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Patient patient = Patient.builder()
                .dpi(request.getDpi())
                .nit(request.getNit())
                .firstName(request.getFirstName())
                .secondName(request.getSecondName())
                .firstLastName(request.getFirstLastName())
                .secondLastName(request.getSecondLastName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .email(request.getEmail())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .municipality(request.getMunicipality())
                .zone(request.getZone())
                .address(request.getAddress())
                .build();

        Patient saved = patientRepository.save(patient);

        // Crear cuenta en auth-service (CU-01: envía usuario + contraseña temporal)
        String tempPassword = authServiceClient.createPatientAccount(
                saved.getDpi(), saved.getEmail(), saved.getFullName());

        if (tempPassword != null) {
            log.info("[CU-01] Cuenta creada para paciente DPI:{}. Contraseña temporal: {}", saved.getDpi(), tempPassword);
        } else {
            log.warn("[CU-01] No se pudo crear cuenta en auth-service para DPI:{}", saved.getDpi());
        }

        return PatientResponse.from(saved);
    }

    public PatientResponse getById(String id) {
        Patient patient = patientRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new PatientNotFoundException(id));
        return PatientResponse.from(patient);
    }

    public PatientResponse getByDpi(String dpi) {
        Patient patient = patientRepository.findByDpi(dpi)
                .orElseThrow(() -> new PatientNotFoundException(dpi));
        return PatientResponse.from(patient);
    }

    public List<PatientResponse> search(String query) {
        return patientRepository.search(query).stream()
                .map(PatientResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientResponse update(String id, UpdatePatientRequest request) {
        Patient patient = patientRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new PatientNotFoundException(id));

        if (request.getEmail() != null && !request.getEmail().equals(patient.getEmail())) {
            if (patientRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateEmailException(request.getEmail());
            }
            patient.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) patient.setPhone(request.getPhone());
        if (request.getDepartment() != null) patient.setDepartment(request.getDepartment());
        if (request.getMunicipality() != null) patient.setMunicipality(request.getMunicipality());
        if (request.getZone() != null) patient.setZone(request.getZone());
        if (request.getAddress() != null) patient.setAddress(request.getAddress());

        return PatientResponse.from(patientRepository.save(patient));
    }
}
