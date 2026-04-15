package com.medflow.patient.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify Flyway migrations execute successfully.
 * Tests that the database schema is created correctly with all tables,
 * constraints, and sample data.
 */
@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateDoctorsTable() {
        // Verify doctors table exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'doctors'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);

        // Verify sample doctors were inserted
        Integer doctorCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM doctors",
            Integer.class
        );
        assertThat(doctorCount).isEqualTo(3);
    }

    @Test
    void shouldCreateDoctorSchedulesTable() {
        // Verify doctor_schedules table exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'doctor_schedules'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);

        // Verify sample schedules were inserted (3 doctors × 5 days = 15 schedules)
        Integer scheduleCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM doctor_schedules",
            Integer.class
        );
        assertThat(scheduleCount).isEqualTo(15);
    }

    @Test
    void shouldHaveDoctorSchedulesForeignKey() {
        // Verify foreign key constraint exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.table_constraints " +
            "WHERE constraint_type = 'FOREIGN KEY' " +
            "AND table_name = 'doctor_schedules' " +
            "AND constraint_name LIKE '%doctor_id%'",
            Integer.class
        );
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void shouldHaveDayOfWeekConstraint() {
        // Verify day_of_week check constraint exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.check_constraints " +
            "WHERE constraint_name = 'chk_day_of_week'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldHaveTimeRangeConstraint() {
        // Verify time_range check constraint exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.check_constraints " +
            "WHERE constraint_name = 'chk_time_range'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldHaveUniqueConstraintOnDoctorSchedule() {
        // Verify unique constraint on (doctor_id, day_of_week, start_time)
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.table_constraints " +
            "WHERE constraint_type = 'UNIQUE' " +
            "AND table_name = 'doctor_schedules' " +
            "AND constraint_name = 'idx_doctor_schedule'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldHaveIndexOnDoctorId() {
        // Verify index on doctor_id exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM pg_indexes " +
            "WHERE tablename = 'doctor_schedules' " +
            "AND indexname = 'idx_doctor_schedules_doctor'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldHaveIndexOnIsActive() {
        // Verify index on is_active exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM pg_indexes " +
            "WHERE tablename = 'doctor_schedules' " +
            "AND indexname = 'idx_doctor_schedules_active'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldHaveCorrectScheduleData() {
        // Verify schedules are Monday-Friday (1-5)
        Integer mondayToFridayCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM doctor_schedules WHERE day_of_week BETWEEN 1 AND 5",
            Integer.class
        );
        assertThat(mondayToFridayCount).isEqualTo(15);

        // Verify schedules are 8:00-17:00
        Integer correctTimeCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM doctor_schedules " +
            "WHERE start_time = '08:00:00' AND end_time = '17:00:00'",
            Integer.class
        );
        assertThat(correctTimeCount).isEqualTo(15);

        // Verify all schedules are active
        Integer activeCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM doctor_schedules WHERE is_active = true",
            Integer.class
        );
        assertThat(activeCount).isEqualTo(15);
    }
}
