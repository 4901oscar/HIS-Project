package com.medflow.billing.service;

import com.medflow.billing.dto.request.ServiceItemRequest;
import com.medflow.billing.dto.response.ServiceItemResponse;
import com.medflow.billing.model.ServiceItem;
import com.medflow.billing.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceItemService {

    private final ServiceItemRepository repository;

    public List<ServiceItemResponse> getAll(String category) {
        List<ServiceItem> items = (category != null && !category.isBlank())
                ? repository.findByCategory(category)
                : repository.findAll();
        return items.stream().map(ServiceItemResponse::from).collect(Collectors.toList());
    }

    public ServiceItemResponse create(ServiceItemRequest req) {
        ServiceItem item = ServiceItem.builder()
                .code(req.getCode().toUpperCase())
                .name(req.getName())
                .description(req.getDescription())
                .category(req.getCategory())
                .price(req.getPrice())
                .active(true)
                .build();
        return ServiceItemResponse.from(repository.save(item));
    }

    public ServiceItemResponse update(String id, ServiceItemRequest req) {
        ServiceItem item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        item.setCode(req.getCode().toUpperCase());
        item.setName(req.getName());
        item.setDescription(req.getDescription());
        item.setCategory(req.getCategory());
        item.setPrice(req.getPrice());
        return ServiceItemResponse.from(repository.save(item));
    }

    public ServiceItemResponse toggleActive(String id) {
        ServiceItem item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        item.setActive(!item.isActive());
        return ServiceItemResponse.from(repository.save(item));
    }
}
