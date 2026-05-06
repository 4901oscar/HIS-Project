package com.medflow.billing.repository;

import com.medflow.billing.model.ServiceItem;
import com.medflow.billing.model.ServiceItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, String> {
    List<ServiceItem> findByStatusNot(ServiceItemStatus status);
    List<ServiceItem> findByCategoryAndStatusNot(String category, ServiceItemStatus status);
    boolean existsByCode(String code);
}
