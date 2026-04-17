package com.medframe.clinical.infrastructure.cache;

import com.medframe.clinical.domain.port.out.AppointmentSlotCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedisAppointmentSlotCache implements AppointmentSlotCache {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String SLOT_KEY_PREFIX = "appointment:slots:";
    private static final long SLOT_TTL_DAYS = 7;

    public RedisAppointmentSlotCache(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Set<LocalTime> getOccupiedSlots(String doctorId, LocalDate date) {
        String key = buildKey(doctorId, date);
        Set<String> slots = redisTemplate.opsForSet().members(key);

        if (slots == null) {
            return Collections.emptySet();
        }

        return slots.stream()
                .map(LocalTime::parse)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean reserveSlot(String doctorId, LocalDate date, LocalTime time) {
        String key = buildKey(doctorId, date);
        String timeStr = time.toString();

        // Atomic operation: add only if not exists
        Long added = redisTemplate.opsForSet().add(key, timeStr);

        if (added != null && added > 0) {
            // Set expiration
            redisTemplate.expire(key, SLOT_TTL_DAYS, TimeUnit.DAYS);
            return true;
        }

        return false;
    }

    @Override
    public void releaseSlot(String doctorId, LocalDate date, LocalTime time) {
        String key = buildKey(doctorId, date);
        String timeStr = time.toString();

        redisTemplate.opsForSet().remove(key, timeStr);
    }

    private String buildKey(String doctorId, LocalDate date) {
        return SLOT_KEY_PREFIX + doctorId + ":" + date.toString();
    }
}
