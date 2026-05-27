package com.medflow.patient.service;

import com.medflow.patient.dto.CreatePatientRequest;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.patient.dto.UpdatePatientRequest;
import com.medflow.patient.exception.DuplicateDpiException;
import com.medflow.patient.exception.DuplicateEmailException;
import com.medflow.patient.exception.PatientNotFoundException;
import com.medflow.patient.model.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PatientServiceIntegrationTest {

    @Autowired
    private PatientService patientService;

    @MockBean
    private AuthServiceClient authServiceClient;

    @BeforeEach
    void setUp() {
        when(authServiceClient.createPatientAccount(
                anyString(), anyString(), anyString(), any(), anyString(), any(), anyString(), any(), any()))
                .thenReturn(new AuthServiceClient.PatientAccountResult("auth-uuid-001", "Temp1234!"));
    }

    private CreatePatientRequest buildRequest(String dpi, String email) {
        CreatePatientRequest req = new CreatePatientRequest();
        req.setDpi(dpi);
        req.setNit("12345678");
        req.setFirstName("Juan");
        req.setFirstLastName("García");
        req.setBirthDate(LocalDate.of(1990, 5, 15));
        req.setGender(Gender.M);
        req.setEmail(email);
        req.setPhone("55551234");
        req.setDepartment("Guatemala");
        req.setMunicipality("Guatemala");
        return req;
    }

    @Test
    void createPatient_guardaDatosYAuthUserId() {
        PatientResponse response = patientService.createPatient(buildRequest("1234567890123", "juan@test.com"), null);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getDpi()).isEqualTo("1234567890123");
        assertThat(response.getEmail()).isEqualTo("juan@test.com");
        assertThat(response.getAuthUserId()).isEqualTo("auth-uuid-001");
    }

    @Test
    void createPatient_lanzaExcepcionSiDpiDuplicado() {
        patientService.createPatient(buildRequest("1234567890123", "juan@test.com"), null);

        assertThatThrownBy(() ->
                patientService.createPatient(buildRequest("1234567890123", "otro@test.com"), null))
                .isInstanceOf(DuplicateDpiException.class);
    }

    @Test
    void createPatient_lanzaExcepcionSiEmailDuplicado() {
        patientService.createPatient(buildRequest("1234567890123", "juan@test.com"), null);

        assertThatThrownBy(() ->
                patientService.createPatient(buildRequest("9876543210987", "juan@test.com"), null))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void getById_retornaPacienteExistente() {
        PatientResponse created = patientService.createPatient(buildRequest("1234567890123", "juan@test.com"), null);

        PatientResponse found = patientService.getById(created.getId());

        assertThat(found.getDpi()).isEqualTo("1234567890123");
    }

    @Test
    void getById_lanzaExcepcionSiNoExiste() {
        assertThatThrownBy(() ->
                patientService.getById("00000000-0000-0000-0000-000000000000"))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void getByDpi_retornaPacienteExistente() {
        patientService.createPatient(buildRequest("1234567890123", "juan@test.com"), null);

        PatientResponse found = patientService.getByDpi("1234567890123");

        assertThat(found.getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    void search_retornaPacientesPorNombre() {
        patientService.createPatient(buildRequest("1234567890123", "juan@test.com"), null);

        List<PatientResponse> results = patientService.search("García");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDpi()).isEqualTo("1234567890123");
    }

    @Test
    void update_modificaCamposPermitidos() {
        PatientResponse created = patientService.createPatient(buildRequest("1234567890123", "juan@test.com"), null);

        UpdatePatientRequest update = new UpdatePatientRequest();
        update.setPhone("99998888");
        update.setAddress("Nueva Dirección 123");

        PatientResponse updated = patientService.update(created.getId(), update, null);

        assertThat(updated.getPhone()).isEqualTo("99998888");
        assertThat(updated.getAddress()).isEqualTo("Nueva Dirección 123");
        assertThat(updated.getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    void update_lanzaExcepcionSiEmailDuplicado() {
        patientService.createPatient(buildRequest("1234567890123", "juan@test.com"), null);
        PatientResponse segundo = patientService.createPatient(buildRequest("9876543210987", "maria@test.com"), null);

        UpdatePatientRequest update = new UpdatePatientRequest();
        update.setEmail("juan@test.com");

        assertThatThrownBy(() ->
                patientService.update(segundo.getId().toString(), update, null))
                .isInstanceOf(DuplicateEmailException.class);
    }
}
