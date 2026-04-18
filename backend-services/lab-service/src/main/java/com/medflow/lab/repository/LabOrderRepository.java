package com.medflow.lab.repository;

import com.medflow.lab.model.LabOrder;
import com.medflow.lab.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, String> {

    List<LabOrder> findByStatus(OrderStatus status);

    List<LabOrder> findByPatientId(String patientId);

    Optional<LabOrder> findByOrderCode(String orderCode);
}
