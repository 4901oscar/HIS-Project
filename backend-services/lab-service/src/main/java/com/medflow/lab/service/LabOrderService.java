package com.medflow.lab.service;

import com.medflow.lab.dto.request.LabOrderNotificationRequest;
import com.medflow.lab.dto.response.LabOrderResponse;
import com.medflow.lab.exception.InvalidOrderStatusException;
import com.medflow.lab.exception.LabOrderNotFoundException;
import com.medflow.lab.model.LabOrder;
import com.medflow.lab.model.OrderStatus;
import com.medflow.lab.model.Sample;
import com.medflow.lab.repository.LabOrderRepository;
import com.medflow.lab.repository.SampleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabOrderService {

    private final LabOrderRepository labOrderRepository;
    private final SampleRepository sampleRepository;

    @Transactional
    public LabOrderResponse receiveOrder(LabOrderNotificationRequest request) {
        log.info("Recibiendo orden de laboratorio: {}", request.getOrderCode());

        LabOrder order = LabOrder.builder()
                .orderCode(request.getOrderCode())
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .testNames(request.getTestNames())
                .status(OrderStatus.PENDING)
                .orderedAt(LocalDateTime.now())
                .build();

        LabOrder savedOrder = labOrderRepository.save(order);
        log.info("Orden guardada con ID: {}", savedOrder.getId());

        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponse> getOrders(OrderStatus status) {
        log.info("Consultando órdenes con estado: {}", status);

        List<LabOrder> orders = status != null 
                ? labOrderRepository.findByStatus(status)
                : labOrderRepository.findAll();

        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LabOrderResponse getOrderById(String id) {
        log.info("Consultando orden por ID: {}", id);

        LabOrder order = labOrderRepository.findById(id)
                .orElseThrow(() -> new LabOrderNotFoundException(
                        "Orden de laboratorio no encontrada con ID: " + id));

        return mapToResponse(order);
    }

    @Transactional
    public LabOrderResponse collectSample(String orderId, String collectedBy) {
        log.info("Registrando toma de muestra para orden: {}", orderId);

        LabOrder order = labOrderRepository.findById(orderId)
                .orElseThrow(() -> new LabOrderNotFoundException(
                        "Orden de laboratorio no encontrada con ID: " + orderId));

        // Validate status transition: PENDING → IN_PROGRESS
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(
                    "Solo las órdenes con estado PENDING pueden pasar a IN_PROGRESS. Estado actual: " + order.getStatus());
        }

        // Update order status
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setUpdatedAt(LocalDateTime.now());
        LabOrder updatedOrder = labOrderRepository.save(order);

        // Create sample record
        Sample sample = Sample.builder()
                .orderId(orderId)
                .collectedBy(collectedBy)
                .collectedAt(LocalDateTime.now())
                .build();
        Sample savedSample = sampleRepository.save(sample);

        log.info("Muestra registrada con ID: {}", savedSample.getId());

        LabOrderResponse response = mapToResponse(updatedOrder);
        response.setSampleId(savedSample.getId());
        return response;
    }

    private LabOrderResponse mapToResponse(LabOrder order) {
        return LabOrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .patientId(order.getPatientId())
                .doctorId(order.getDoctorId())
                .testNames(order.getTestNames())
                .status(order.getStatus())
                .orderedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
