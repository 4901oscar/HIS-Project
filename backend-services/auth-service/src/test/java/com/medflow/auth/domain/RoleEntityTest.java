package com.medflow.auth.domain;

import com.medflow.auth.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/test-data.sql", config = @SqlConfig(encoding = "UTF-8"))
class RoleEntityTest {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Test
    void shouldLoadAllEightRoles() {
        // When
        List<Role> roles = roleRepository.findAll();
        
        // Then
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
    void shouldFindRoleByName() {
        // When
        Optional<Role> doctorRole = roleRepository.findByName(RoleName.DOCTOR);
        
        // Then
        assertThat(doctorRole).isPresent();
        assertThat(doctorRole.get().getName()).isEqualTo(RoleName.DOCTOR);
        assertThat(doctorRole.get().getDescription()).isEqualTo("Médico");
    }
    
    @Test
    void shouldFindAdminRole() {
        // When
        Optional<Role> adminRole = roleRepository.findByName(RoleName.ADMIN);
        
        // Then
        assertThat(adminRole).isPresent();
        assertThat(adminRole.get().getName()).isEqualTo(RoleName.ADMIN);
        assertThat(adminRole.get().getDescription()).isEqualTo("Súper Usuario");
    }
    
    @Test
    void shouldFindPatientRole() {
        // When
        Optional<Role> patientRole = roleRepository.findByName(RoleName.PATIENT);
        
        // Then
        assertThat(patientRole).isPresent();
        assertThat(patientRole.get().getName()).isEqualTo(RoleName.PATIENT);
        assertThat(patientRole.get().getDescription()).isEqualTo("Paciente");
    }
}
