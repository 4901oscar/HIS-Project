package com.medframe.clinical.infrastructure.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class RedisAppointmentSlotCacheIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private RedisAppointmentSlotCache cache;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void shouldReserveAndRetrieveSlots() {
        // Given
        String doctorId = "doctor123";
        LocalDate date = LocalDate.of(2024, 4, 15);
        LocalTime slot1 = LocalTime.of(9, 0);
        LocalTime slot2 = LocalTime.of(10, 30);

        // When - Reserve slots
        boolean reserved1 = cache.reserveSlot(doctorId, date, slot1);
        boolean reserved2 = cache.reserveSlot(doctorId, date, slot2);

        // Then - Both should be reserved
        assertThat(reserved1).isTrue();
        assertThat(reserved2).isTrue();

        // When - Retrieve occupied slots
        Set<LocalTime> occupiedSlots = cache.getOccupiedSlots(doctorId, date);

        // Then - Should contain both slots
        assertThat(occupiedSlots).containsExactlyInAnyOrder(slot1, slot2);
    }

    @Test
    void shouldNotReserveSameSlotTwice() {
        // Given
        String doctorId = "doctor456";
        LocalDate date = LocalDate.of(2024, 4, 16);
        LocalTime slot = LocalTime.of(14, 0);

        // When - Reserve slot twice
        boolean firstReservation = cache.reserveSlot(doctorId, date, slot);
        boolean secondReservation = cache.reserveSlot(doctorId, date, slot);

        // Then
        assertThat(firstReservation).isTrue();
        assertThat(secondReservation).isFalse();
    }

    @Test
    void shouldReleaseSlot() {
        // Given
        String doctorId = "doctor789";
        LocalDate date = LocalDate.of(2024, 4, 17);
        LocalTime slot = LocalTime.of(11, 0);

        // When - Reserve and then release
        cache.reserveSlot(doctorId, date, slot);
        cache.releaseSlot(doctorId, date, slot);

        // Then - Slot should not be occupied
        Set<LocalTime> occupiedSlots = cache.getOccupiedSlots(doctorId, date);
        assertThat(occupiedSlots).doesNotContain(slot);
    }

    @Test
    void shouldReturnEmptySetForDoctorWithNoSlots() {
        // Given
        String doctorId = "doctorWithNoSlots";
        LocalDate date = LocalDate.of(2024, 4, 18);

        // When
        Set<LocalTime> occupiedSlots = cache.getOccupiedSlots(doctorId, date);

        // Then
        assertThat(occupiedSlots).isEmpty();
    }

    @Test
    void shouldIsolateSlotsPerDoctorAndDate() {
        // Given
        String doctor1 = "doctor1";
        String doctor2 = "doctor2";
        LocalDate date1 = LocalDate.of(2024, 4, 19);
        LocalDate date2 = LocalDate.of(2024, 4, 20);
        LocalTime slot = LocalTime.of(15, 0);

        // When - Reserve same slot for different doctors and dates
        cache.reserveSlot(doctor1, date1, slot);
        cache.reserveSlot(doctor2, date1, slot);
        cache.reserveSlot(doctor1, date2, slot);

        // Then - Each should have their own slot
        assertThat(cache.getOccupiedSlots(doctor1, date1)).contains(slot);
        assertThat(cache.getOccupiedSlots(doctor2, date1)).contains(slot);
        assertThat(cache.getOccupiedSlots(doctor1, date2)).contains(slot);
        assertThat(cache.getOccupiedSlots(doctor2, date2)).doesNotContain(slot);
    }

    @Test
    void shouldUseCorrectKeyPattern() {
        // Given
        String doctorId = "doctor999";
        LocalDate date = LocalDate.of(2024, 5, 1);
        LocalTime slot = LocalTime.of(16, 30);
        String expectedKey = "appointment:slots:doctor999:2024-05-01";

        // When
        cache.reserveSlot(doctorId, date, slot);

        // Then - Verify key exists in Redis
        Boolean keyExists = redisTemplate.hasKey(expectedKey);
        assertThat(keyExists).isTrue();

        // Verify the value
        Set<String> members = redisTemplate.opsForSet().members(expectedKey);
        assertThat(members).contains("16:30");
    }
}
