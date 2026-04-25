package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DiscriminatorRequest {

    @NotBlank
    @Size(max = 10)
    private String code;

    @NotBlank
    @Size(max = 200)
    private String description;

    @NotBlank
    private String priorityLevel;

    private String motifId;
}
