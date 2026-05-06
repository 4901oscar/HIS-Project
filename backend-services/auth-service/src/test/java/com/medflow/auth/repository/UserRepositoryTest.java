package com.medflow.auth.repository;

import com.medflow.auth.domain.Role;
import com.medflow.auth.domain.RoleName;
import com.medflow.auth.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role doctorRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.deleteAll();
        
        // Ensure roles exist (they should be created by Hibernate)
        if (roleRepository.findByName(RoleName.DOCTOR).isEmpty()) {
            doctorRole = roleRepository.save(Role.builder()
                    .name(RoleName.DOCTOR)
                    .description("Médico")
                    .build());
        } else {
            doctorRole = roleRepository.findByName(RoleName.DOCTOR)
                    .orElseThrow(() -> new RuntimeException("DOCTOR role not found"));
        }
        
        if (roleRepository.findByName(RoleName.ADMIN).isEmpty()) {
            adminRole = roleRepository.save(Role.builder()
                    .name(RoleName.ADMIN)
                    .description("Súper Usuario")
                    .build());
        } else {
            adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        }
    }

    @Test
    void shouldSaveUser() {
        // Given
        User user = User.builder()
                .username("doctor1")
                .password("hashedPassword123")
                .email("doctor1@medflow.com")
                .firstName("Juan")
                .firstLastName("Pérez")
                .active(true)
                .roles(Set.of(doctorRole))
                .build();

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("doctor1");
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword123");
        assertThat(savedUser.getEmail()).isEqualTo("doctor1@medflow.com");
        assertThat(savedUser.getFullName()).isEqualTo("Juan Pérez");
        assertThat(savedUser.isActive()).isTrue();
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles()).contains(doctorRole);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByUsername() {
        // Given
        User user = User.builder()
                .username("doctor2")
                .password("hashedPassword456")
                .email("doctor2@medflow.com")
                .firstName("María")
                .firstLastName("García")
                .active(true)
                .roles(Set.of(doctorRole, adminRole))
                .build();
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByUsername("doctor2");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("doctor2");
        assertThat(foundUser.get().getEmail()).isEqualTo("doctor2@medflow.com");
        assertThat(foundUser.get().getFullName()).isEqualTo("María García");
        assertThat(foundUser.get().getRoles()).hasSize(2);
        assertThat(foundUser.get().getRoles()).containsExactlyInAnyOrder(doctorRole, adminRole);
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        User user = User.builder()
                .username("doctor3")
                .password("hashedPassword789")
                .email("doctor3@medflow.com")
                .firstName("Carlos")
                .firstLastName("López")
                .active(true)
                .roles(Set.of(doctorRole))
                .build();
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByEmail("doctor3@medflow.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("doctor3");
        assertThat(foundUser.get().getEmail()).isEqualTo("doctor3@medflow.com");
        assertThat(foundUser.get().getFullName()).isEqualTo("Dr. Carlos López");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // When
        Optional<User> foundByUsername = userRepository.findByUsername("nonexistent");
        Optional<User> foundByEmail = userRepository.findByEmail("nonexistent@medflow.com");

        // Then
        assertThat(foundByUsername).isEmpty();
        assertThat(foundByEmail).isEmpty();
    }
}
