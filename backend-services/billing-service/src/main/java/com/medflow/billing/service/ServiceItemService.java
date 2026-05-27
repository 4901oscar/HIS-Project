package com.medflow.billing.service;

import com.medflow.billing.dto.request.ServiceItemRequest;
import com.medflow.billing.dto.response.ServiceItemResponse;
import com.medflow.billing.model.ServiceItem;
import com.medflow.billing.model.ServiceItemStatus;
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
                ? repository.findByCategoryAndStatusNot(category, ServiceItemStatus.DELETED)
                : repository.findByStatusNot(ServiceItemStatus.DELETED);
        return items.stream().map(ServiceItemResponse::from).collect(Collectors.toList());
    }

    public ServiceItemResponse create(ServiceItemRequest req, String userId) {
        ServiceItem item = ServiceItem.builder()
                .code(req.getCode().toUpperCase())
                .name(req.getName())
                .description(req.getDescription())
                .category(req.getCategory())
                .price(req.getPrice())
                .status(ServiceItemStatus.ACTIVE)
                .createdBy(userId != null ? userId : "internal")
                .build();
        return ServiceItemResponse.from(repository.save(item));
    }

    public ServiceItemResponse update(String id, ServiceItemRequest req, String userId) {
        ServiceItem item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        item.setCode(req.getCode().toUpperCase());
        item.setName(req.getName());
        item.setDescription(req.getDescription());
        item.setCategory(req.getCategory());
        item.setPrice(req.getPrice());
        if (req.getStatus() != null) {
            item.setStatus(req.getStatus());
        }
        item.setUpdatedBy(userId != null ? userId : "internal");
        return ServiceItemResponse.from(repository.save(item));
    }

    public ServiceItemResponse toggleActive(String id, String userId) {
        ServiceItem item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        if (item.getStatus() == ServiceItemStatus.DELETED) {
            throw new IllegalStateException("No se puede cambiar el estado de un servicio eliminado");
        }
        ServiceItemStatus next = item.getStatus() == ServiceItemStatus.ACTIVE
                ? ServiceItemStatus.INACTIVE
                : ServiceItemStatus.ACTIVE;
        item.setStatus(next);
        item.setUpdatedBy(userId != null ? userId : "internal");
        return ServiceItemResponse.from(repository.save(item));
    }

    public void delete(String id, String userId) {
        ServiceItem item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        item.setStatus(ServiceItemStatus.DELETED);
        item.setUpdatedBy(userId != null ? userId : "internal");
        repository.save(item);
    }
}
