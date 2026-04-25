package com.medframe.clinical.infrastructure.cache;

import com.medframe.clinical.domain.port.out.AppointmentSlotCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedisAppointmentSlotCache implements AppointmentSlotCache {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String SLOT_KEY_PREFIX    = "appointment:slots:";
    private static final String HOLD_KEY_PREFIX    = "appointment:hold:";
    private static final String SESSION_KEY_PREFIX = "appointment:hold:session:";
    private static final long   SLOT_TTL_DAYS      = 7;
    private static final long   HOLD_TTL_SECONDS   = 600; // 10 minutes

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

    @Override
    public boolean isSlotOccupied(String doctorId, LocalDate date, LocalTime time) {
        String key = buildKey(doctorId, date);
        String timeStr = time.toString();
        
        Boolean isMember = redisTemplate.opsForSet().isMember(key, timeStr);
        return isMember != null && isMember;
    }
    
    @Override
    public void clearSlots(String doctorId, LocalDate date) {
        String key = buildKey(doctorId, date);
        redisTemplate.delete(key);
    }

    @Override
    public boolean holdTimeSlot(String sessionId, LocalDate date, LocalTime time) {
        // Release whatever this session was previously holding
        releaseTimeSlotHold(sessionId);

        String holdKey    = HOLD_KEY_PREFIX + date + ":" + time;
        String sessionKey = SESSION_KEY_PREFIX + sessionId;

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(holdKey, sessionId, HOLD_TTL_SECONDS, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(acquired)) {
            redisTemplate.opsForValue().set(sessionKey, date + ":" + time, HOLD_TTL_SECONDS, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    @Override
    public void releaseTimeSlotHold(String sessionId) {
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        String held = redisTemplate.opsForValue().get(sessionKey);
        if (held != null) {
            String holdKey = HOLD_KEY_PREFIX + held;
            String owner   = redisTemplate.opsForValue().get(holdKey);
            if (sessionId.equals(owner)) {
                redisTemplate.delete(holdKey);
            }
            redisTemplate.delete(sessionKey);
        }
    }

    @Override
    public Set<LocalTime> getHeldByOthers(String sessionId, LocalDate date) {
        String pattern = HOLD_KEY_PREFIX + date + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) return Collections.emptySet();

        Set<LocalTime> result = new HashSet<>();
        String prefix = HOLD_KEY_PREFIX + date + ":";
        for (String key : keys) {
            String owner = redisTemplate.opsForValue().get(key);
            if (owner != null && !owner.equals(sessionId)) {
                String timeStr = key.substring(prefix.length());
                result.add(LocalTime.parse(timeStr));
            }
        }
        return result;
    }

    private String buildKey(String doctorId, LocalDate date) {
        return SLOT_KEY_PREFIX + doctorId + ":" + date.toString();
    }
}
