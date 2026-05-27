package com.medframe.clinical.infrastructure.cache;

import com.medframe.clinical.domain.port.out.AppointmentSlotCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RedisAppointmentSlotCache implements AppointmentSlotCache {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String SLOT_KEY_PREFIX    = "appointment:slots:";
    private static final String HOLD_KEY_PREFIX    = "appointment:hold:";
    private static final String SESSION_KEY_PREFIX = "appointment:hold:session:";
    private static final long   SLOT_TTL_DAYS      = 7;
    private static final long   HOLD_TTL_SECONDS   = 600;

    public RedisAppointmentSlotCache(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Set<LocalTime> getOccupiedSlots(String doctorId, LocalDate date) {
        try {
            String key = buildKey(doctorId, date);
            Set<String> slots = redisTemplate.opsForSet().members(key);
            if (slots == null) return Collections.emptySet();
            return slots.stream().map(LocalTime::parse).collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("[Redis] getOccupiedSlots falló, asumiendo sin ocupados: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    @Override
    public boolean reserveSlot(String doctorId, LocalDate date, LocalTime time) {
        try {
            String key = buildKey(doctorId, date);
            Long added = redisTemplate.opsForSet().add(key, time.toString());
            if (added != null && added > 0) {
                redisTemplate.expire(key, SLOT_TTL_DAYS, TimeUnit.DAYS);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("[Redis] reserveSlot falló: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void releaseSlot(String doctorId, LocalDate date, LocalTime time) {
        try {
            redisTemplate.opsForSet().remove(buildKey(doctorId, date), time.toString());
        } catch (Exception e) {
            log.warn("[Redis] releaseSlot falló: {}", e.getMessage());
        }
    }

    @Override
    public boolean isSlotOccupied(String doctorId, LocalDate date, LocalTime time) {
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(buildKey(doctorId, date), time.toString());
            return Boolean.TRUE.equals(isMember);
        } catch (Exception e) {
            log.warn("[Redis] isSlotOccupied falló, asumiendo libre: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void clearSlots(String doctorId, LocalDate date) {
        try {
            redisTemplate.delete(buildKey(doctorId, date));
        } catch (Exception e) {
            log.warn("[Redis] clearSlots falló: {}", e.getMessage());
        }
    }

    @Override
    public boolean holdTimeSlot(String sessionId, LocalDate date, LocalTime time) {
        try {
            releaseTimeSlotHold(sessionId);
            String holdKey    = HOLD_KEY_PREFIX + date + ":" + time;
            String sessionKey = SESSION_KEY_PREFIX + sessionId;
            Boolean acquired  = redisTemplate.opsForValue()
                    .setIfAbsent(holdKey, sessionId, HOLD_TTL_SECONDS, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(acquired)) {
                redisTemplate.opsForValue().set(sessionKey, date + ":" + time, HOLD_TTL_SECONDS, TimeUnit.SECONDS);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("[Redis] holdTimeSlot falló: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void releaseTimeSlotHold(String sessionId) {
        try {
            String sessionKey = SESSION_KEY_PREFIX + sessionId;
            String held = redisTemplate.opsForValue().get(sessionKey);
            if (held != null) {
                String holdKey = HOLD_KEY_PREFIX + held;
                String owner   = redisTemplate.opsForValue().get(holdKey);
                if (sessionId.equals(owner)) redisTemplate.delete(holdKey);
                redisTemplate.delete(sessionKey);
            }
        } catch (Exception e) {
            log.warn("[Redis] releaseTimeSlotHold falló: {}", e.getMessage());
        }
    }

    @Override
    public Set<LocalTime> getHeldByOthers(String sessionId, LocalDate date) {
        try {
            String pattern = HOLD_KEY_PREFIX + date + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) return Collections.emptySet();

            Set<LocalTime> result = new HashSet<>();
            String prefix = HOLD_KEY_PREFIX + date + ":";
            for (String key : keys) {
                String owner = redisTemplate.opsForValue().get(key);
                if (owner != null && !owner.equals(sessionId)) {
                    result.add(LocalTime.parse(key.substring(prefix.length())));
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("[Redis] getHeldByOthers falló, asumiendo sin holds: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    private String buildKey(String doctorId, LocalDate date) {
        return SLOT_KEY_PREFIX + doctorId + ":" + date;
    }
}
