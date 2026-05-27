package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class HoldSlotRequest {

    @NotBlank
    private String sessionId;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime time;
}
