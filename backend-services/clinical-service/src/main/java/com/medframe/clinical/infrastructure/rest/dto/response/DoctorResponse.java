package com.medframe.clinical.infrastructure.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for doctor information.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    
    private String id;
    private String name;
    private String shiftStart;  // Format: HH:mm
    private String shiftEnd;    // Format: HH:mm
    private String status;      // ACTIVE or INACTIVE
    private String clinicId;
    private LocalDateTime createdAt;
}
