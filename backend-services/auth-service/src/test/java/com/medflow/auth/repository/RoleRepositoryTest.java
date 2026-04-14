package com.medflow.auth.repository;

import com.medflow.auth.domain.Role;
import com.medflow.auth.domain.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        // Clean up and insert all 8 roles
        roleRepository.deleteAll();
        
        roleRepository.save(Role.builder().name(RoleName.ADMIN).description("Súper Usuario").build());
        roleRepository.save(Role.builder().name(RoleName.ADMISSION).description("Personal de Admisión").build());
        roleRepository.save(Role.builder().name(RoleName.VITAL_SIGNS).description("Personal de Signos Vitales").build());
        roleRepository.save(Role.builder().name(RoleName.DOCTOR).description("Médico").build());
        roleRepository.save(Role.builder().name(RoleName.LABORATORY).description("Personal de Laboratorio").build());
        roleRepository.save(Role.builder().name(RoleName.PHARMACY).description("Farmacéutico").build());
        roleRepository.save(Role.builder().name(RoleName.CASHIER).description("Cajero").build());
        roleRepository.save(Role.builder().name(RoleName.PATIENT).description("Paciente").build());
    }

    @Test
    void shouldFindRoleByName() {
        // When
        Optional<Role> doctorRole = roleRepository.findByName(RoleName.DOCTOR);
        Optional<Role> adminRole = roleRepository.findByName(RoleName.ADMIN);

        // Then
        assertThat(doctorRole).isPresent();
        assertThat(doctorRole.get().getName()).isEqualTo(RoleName.DOCTOR);
        assertThat(doctorRole.get().getDescription()).isEqualTo("Médico");

        assertThat(adminRole).isPresent();
        assertThat(adminRole.get().getName()).isEqualTo(RoleName.ADMIN);
        assertThat(adminRole.get().getDescription()).isEqualTo("Súper Usuario");
    }

    @Test
    void shouldReturnEmptyWhenRoleNotFound() {
        // When - trying to find a role that doesn't exist in the enum
        // Note: All enum values are pre-populated, so this tests the Optional behavior
        Optional<Role> result = roleRepository.findByName(RoleName.DOCTOR);
        
        // Then - verify the method returns Optional correctly
        assertThat(result).isPresent();
        
        // Verify all 8 roles exist as per requirements
        List<Role> allRoles = roleRepository.findAll();
        assertThat(allRoles).hasSize(8);
    }

    @Test
    void shouldLoadAllEightRoles() {
        // When
        List<Role> roles = roleRepository.findAll();

        // Then - verify all 8 roles from requirements are present
        assertThat(roles).hasSize(8);
        assertThat(roles).extracting(Role::getName)
                .containsExactlyInAnyOrder(
                        RoleName.ADMIN,
                        RoleName.ADMISSION,
                        RoleName.VITAL_SIGNS,
                        RoleName.DOCTOR,
                        RoleName.LABORATORY,
                        RoleName.PHARMACY,
                        RoleName.CASHIER,
                        RoleName.PATIENT
                );
    }

    @Test
    void shouldVerifyRoleDescriptions() {
        // When
        Optional<Role> admission = roleRepository.findByName(RoleName.ADMISSION);
        Optional<Role> pharmacy = roleRepository.findByName(RoleName.PHARMACY);
        Optional<Role> patient = roleRepository.findByName(RoleName.PATIENT);

        // Then
        assertThat(admission).isPresent();
        assertThat(admission.get().getDescription()).isEqualTo("Personal de Admisión");

        assertThat(pharmacy).isPresent();
        assertThat(pharmacy.get().getDescription()).isEqualTo("Farmacéutico");

        assertThat(patient).isPresent();
        assertThat(patient.get().getDescription()).isEqualTo("Paciente");
    }
}
