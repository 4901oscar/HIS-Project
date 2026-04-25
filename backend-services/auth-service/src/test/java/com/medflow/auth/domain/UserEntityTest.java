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
        User user = User.builder()
            .username("doctor1")
            .password("hashedPassword123")
            .email("doctor1@medflow.com")
            .firstName("Juan")
            .firstLastName("Pérez")
            .active(true)
            .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("doctor1");
        assertThat(savedUser.getEmail()).isEqualTo("doctor1@medflow.com");
        assertThat(savedUser.getFullName()).isEqualTo("Juan Pérez");
        assertThat(savedUser.isActive()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByUsername() {
        User user = User.builder()
            .username("testuser")
            .password("password")
            .email("test@medflow.com")
            .firstName("Test")
            .firstLastName("User")
            .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldFindUserByEmail() {
        User user = User.builder()
            .username("emailuser")
            .password("password")
            .email("email@medflow.com")
            .firstName("Email")
            .firstLastName("User")
            .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("email@medflow.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("email@medflow.com");
    }

    @Test
    void shouldAssignRolesToUser() {
        Role doctorRole = roleRepository.findByName(RoleName.DOCTOR)
            .orElseThrow(() -> new RuntimeException("DOCTOR role not found"));
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
            .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        User user = User.builder()
            .username("multirole")
            .password("password")
            .email("multirole@medflow.com")
            .firstName("Multi")
            .firstLastName("User")
            .roles(Set.of(doctorRole, adminRole))
            .build();

        User savedUser = userRepository.save(user);
        User retrievedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(retrievedUser.getRoles()).hasSize(2);
        assertThat(retrievedUser.getRoles()).extracting(Role::getName)
            .containsExactlyInAnyOrder(RoleName.DOCTOR, RoleName.ADMIN);
    }

    @Test
    void shouldGetRolesAsString() {
        Role doctorRole = roleRepository.findByName(RoleName.DOCTOR)
            .orElseThrow(() -> new RuntimeException("DOCTOR role not found"));
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
            .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        User user = User.builder()
            .username("rolestring")
            .password("password")
            .email("rolestring@medflow.com")
            .firstName("Role")
            .firstLastName("User")
            .roles(Set.of(doctorRole, adminRole))
            .build();

        User savedUser = userRepository.save(user);
        String rolesString = savedUser.getRolesAsString();

        assertThat(rolesString).contains("DOCTOR");
        assertThat(rolesString).contains("ADMIN");
        assertThat(rolesString).contains(",");
    }
}
