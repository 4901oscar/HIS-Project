package com.medframe.clinical.infrastructure.rest.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for day-off records.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public record DayOffResponse(
    String id,
    String doctorId,
    LocalDate date,
    boolean isAvailable,
    String reason,
    LocalDateTime createdAt,
    String createdBy
) {}
