package com.medframe.clinical.infrastructure.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisAppointmentSlotCacheTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    private RedisAppointmentSlotCache cache;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        cache = new RedisAppointmentSlotCache(redisTemplate);
    }

    @Test
    void getOccupiedSlots_shouldReturnEmptySetWhenNoSlots() {
        // Given
        String doctorId = "doctor123";
        LocalDate date = LocalDate.of(2024, 4, 15);
        String expectedKey = "appointment:slots:doctor123:2024-04-15";

        when(setOperations.members(expectedKey)).thenReturn(null);

        // When
        Set<LocalTime> result = cache.getOccupiedSlots(doctorId, date);

        // Then
        assertThat(result).isEmpty();
        verify(setOperations).members(expectedKey);
    }

    @Test
    void getOccupiedSlots_shouldReturnParsedLocalTimes() {
        // Given
        String doctorId = "doctor123";
        LocalDate date = LocalDate.of(2024, 4, 15);
        String expectedKey = "appointment:slots:doctor123:2024-04-15";

        Set<String> redisSlots = Set.of("08:00", "09:30", "14:00");
        when(setOperations.members(expectedKey)).thenReturn(redisSlots);

        // When
        Set<LocalTime> result = cache.getOccupiedSlots(doctorId, date);

        // Then
        assertThat(result).containsExactlyInAnyOrder(
                LocalTime.of(8, 0),
                LocalTime.of(9, 30),
                LocalTime.of(14, 0)
        );
        verify(setOperations).members(expectedKey);
    }

    @Test
    void reserveSlot_shouldReturnTrueWhenSlotIsReserved() {
        // Given
        String doctorId = "doctor123";
        LocalDate date = LocalDate.of(2024, 4, 15);
        LocalTime time = LocalTime.of(10, 0);
        String expectedKey = "appointment:slots:doctor123:2024-04-15";
        String timeStr = "10:00";

        when(setOperations.add(expectedKey, timeStr)).thenReturn(1L);
        when(redisTemplate.expire(expectedKey, 7, TimeUnit.DAYS)).thenReturn(true);

        // When
        boolean result = cache.reserveSlot(doctorId, date, time);

        // Then
        assertThat(result).isTrue();
        verify(setOperations).add(expectedKey, timeStr);
        verify(redisTemplate).expire(expectedKey, 7, TimeUnit.DAYS);
    }

    @Test
    void reserveSlot_shouldReturnFalseWhenSlotAlreadyExists() {
        // Given
        String doctorId = "doctor123";
        LocalDate date = LocalDate.of(2024, 4, 15);
        LocalTime time = LocalTime.of(10, 0);
        String expectedKey = "appointment:slots:doctor123:2024-04-15";
        String timeStr = "10:00";

        when(setOperations.add(expectedKey, timeStr)).thenReturn(0L);

        // When
        boolean result = cache.reserveSlot(doctorId, date, time);

        // Then
        assertThat(result).isFalse();
        verify(setOperations).add(expectedKey, timeStr);
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void releaseSlot_shouldRemoveSlotFromRedis() {
        // Given
        String doctorId = "doctor123";
        LocalDate date = LocalDate.of(2024, 4, 15);
        LocalTime time = LocalTime.of(10, 0);
        String expectedKey = "appointment:slots:doctor123:2024-04-15";
        String timeStr = "10:00";

        when(setOperations.remove(expectedKey, timeStr)).thenReturn(1L);

        // When
        cache.releaseSlot(doctorId, date, time);

        // Then
        verify(setOperations).remove(expectedKey, timeStr);
    }

    @Test
    void buildKey_shouldFollowCorrectPattern() {
        // Given
        String doctorId = "doctor456";
        LocalDate date = LocalDate.of(2024, 12, 25);
        LocalTime time = LocalTime.of(15, 30);
        String expectedKey = "appointment:slots:doctor456:2024-12-25";

        when(setOperations.add(expectedKey, "15:30")).thenReturn(1L);
        when(redisTemplate.expire(expectedKey, 7, TimeUnit.DAYS)).thenReturn(true);

        // When
        cache.reserveSlot(doctorId, date, time);

        // Then
        verify(setOperations).add(expectedKey, "15:30");
    }
}
