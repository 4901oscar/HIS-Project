package com.medflow.billing.dto.response;

import com.medflow.billing.model.ServiceItem;
import com.medflow.billing.model.ServiceItemStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceItemResponse {
    private String id;
    private String code;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private ServiceItemStatus status;

    public static ServiceItemResponse from(ServiceItem s) {
        ServiceItemResponse r = new ServiceItemResponse();
        r.setId(s.getId());
        r.setCode(s.getCode());
        r.setName(s.getName());
        r.setDescription(s.getDescription());
        r.setCategory(s.getCategory());
        r.setPrice(s.getPrice());
        r.setStatus(s.getStatus());
        return r;
    }
}
