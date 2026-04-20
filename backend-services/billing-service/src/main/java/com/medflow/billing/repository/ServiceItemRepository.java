package com.medflow.billing.repository;

import com.medflow.billing.model.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, String> {
    List<ServiceItem> findByCategory(String category);
    boolean existsByCode(String code);
}
