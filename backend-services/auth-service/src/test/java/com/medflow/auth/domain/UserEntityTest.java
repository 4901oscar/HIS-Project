package com.medflow.auth.domain;

import com.medflow.auth.repository.RoleRepository;
import com.medflow.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/test-data.sql", config = @SqlConfig(encoding = "UTF-8"))
class UserEntityTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Test
    void shouldSaveAndRetrieveUser() {
        // Given
        User user = User.builder()
            .username("doctor1")
            .password("hashedPassword123")
            .email("doctor1@medflow.com")
            .fullName("Dr. Juan Pérez")
            .active(true)
            .build();
        
        // When
        User savedUser = userRepository.save(user);
        
        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("doctor1");
        assertThat(savedUser.getEmail()).isEqualTo("doctor1@medflow.com");
        assertThat(savedUser.getFullName()).isEqualTo("Dr. Juan Pérez");
        assertThat(savedUser.isActive()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }
    
    @Test
    void shouldFindUserByUsername() {
        // Given
        User user = User.builder()
            .username("testuser")
            .password("password")
            .email("test@medflow.com")
            .fullName("Test User")
            .build();
        userRepository.save(user);
        
        // When
        Optional<User> found = userRepository.findByUsername("testuser");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }
    
    @Test
    void shouldFindUserByEmail() {
        // Given
        User user = User.builder()
            .username("emailuser")
            .password("password")
            .email("email@medflow.com")
            .fullName("Email User")
            .build();
        userRepository.save(user);
        
        // When
        Optional<User> found = userRepository.findByEmail("email@medflow.com");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("email@medflow.com");
    }
    
    @Test
    void shouldAssignRolesToUser() {
        // Given
        Role doctorRole = roleRepository.findByName(RoleName.DOCTOR)
            .orElseThrow(() -> new RuntimeException("DOCTOR role not found"));
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
            .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        
        User user = User.builder()
            .username("multirole")
            .password("password")
            .email("multirole@medflow.com")
            .fullName("Multi Role User")
            .roles(Set.of(doctorRole, adminRole))
            .build();
        
        // When
        User savedUser = userRepository.save(user);
        User retrievedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        
        // Then
        assertThat(retrievedUser.getRoles()).hasSize(2);
        assertThat(retrievedUser.getRoles()).extracting(Role::getName)
            .containsExactlyInAnyOrder(RoleName.DOCTOR, RoleName.ADMIN);
    }
    
    @Test
    void shouldGetRolesAsString() {
        // Given
        Role doctorRole = roleRepository.findByName(RoleName.DOCTOR)
            .orElseThrow(() -> new RuntimeException("DOCTOR role not found"));
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
            .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        
        User user = User.builder()
            .username("rolestring")
            .password("password")
            .email("rolestring@medflow.com")
            .fullName("Role String User")
            .roles(Set.of(doctorRole, adminRole))
            .build();
        
        User savedUser = userRepository.save(user);
        
        // When
        String rolesString = savedUser.getRolesAsString();
        
        // Then
        assertThat(rolesString).contains("DOCTOR");
        assertThat(rolesString).contains("ADMIN");
        assertThat(rolesString).contains(",");
    }
}
