package com.medflow.billing.dto.request;

import com.medflow.billing.model.ServiceItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceItemRequest {
    @NotBlank private String code;
    @NotBlank private String name;
    private String description;
    @NotBlank private String category;
    @NotNull @DecimalMin("0.01") private BigDecimal price;
    private ServiceItemStatus status;
}
