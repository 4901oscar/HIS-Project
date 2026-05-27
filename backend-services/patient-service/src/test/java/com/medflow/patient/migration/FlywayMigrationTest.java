package com.medflow.patient.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreatePatientsTable() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_schema = 'patient_schema' AND table_name = 'patients'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldHaveUniqueIndexOnDpi() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM pg_indexes " +
            "WHERE schemaname = 'patient_schema' AND tablename = 'patients' " +
            "AND indexname = 'idx_patients_dpi'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldHaveUniqueIndexOnEmail() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM pg_indexes " +
            "WHERE schemaname = 'patient_schema' AND tablename = 'patients' " +
            "AND indexname = 'idx_patients_email'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldNotHaveStaleAppointmentTables() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_schema = 'patient_schema' " +
            "AND table_name IN ('doctors','doctor_schedules','appointments')",
            Integer.class
        );
        assertThat(count).isZero();
    }
}
