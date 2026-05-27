package com.medflow.patient.controller;

import com.medflow.patient.dto.CreatePatientInternalRequest;
import com.medflow.patient.dto.CreatePatientRequest;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.patient.dto.UpdatePatientRequest;
import com.medflow.patient.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /** CU-01: Admisión registra paciente presencialmente. */
    @PostMapping
    @PreAuthorize("hasRole('ADMISSION')")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody CreatePatientRequest request, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(request, userId));
    }

    /**
     * Endpoint interno para crear paciente desde auth-service.
     * Este endpoint NO requiere autenticación ya que es solo para comunicación interna entre servicios.
     * El auth-service llama a este endpoint después de crear el usuario en auth_schema.users.
     */
    @PostMapping("/internal")
    public ResponseEntity<PatientResponse> createPatientInternal(@Valid @RequestBody CreatePatientInternalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatientInternal(request));
    }

    /** Obtener paciente por ID. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMISSION','DOCTOR','VITAL_SIGNS','LABORATORY','PHARMACY','CASHIER','PATIENT')")
    public ResponseEntity<PatientResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    /** Buscar por DPI exacto. */
    @GetMapping("/dpi/{dpi}")
    @PreAuthorize("hasAnyRole('ADMISSION','DOCTOR','VITAL_SIGNS','LABORATORY','PHARMACY','CASHIER','PATIENT')")
    public ResponseEntity<PatientResponse> getByDpi(@PathVariable String dpi) {
        return ResponseEntity.ok(patientService.getByDpi(dpi));
    }

    /** Buscar paciente por auth_user_id (para uso interno de clinical-service). */
    @GetMapping("/by-auth-user/{authUserId}")
    public ResponseEntity<PatientResponse> getByAuthUserId(@PathVariable String authUserId) {
        return ResponseEntity.ok(patientService.getByAuthUserId(authUserId));
    }

    /** Búsqueda por nombre, DPI o email (máx 50 resultados). */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMISSION','DOCTOR','VITAL_SIGNS','LABORATORY','PHARMACY','CASHIER','PATIENT')")
    public ResponseEntity<List<PatientResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(patientService.search(query));
    }

    /** Actualizar datos permitidos (email, teléfono, dirección). */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMISSION','PATIENT')")
    public ResponseEntity<PatientResponse> update(@PathVariable String id,
                                                   @Valid @RequestBody UpdatePatientRequest request,
                                                   @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(patientService.update(id, request, userId));
    }
}
