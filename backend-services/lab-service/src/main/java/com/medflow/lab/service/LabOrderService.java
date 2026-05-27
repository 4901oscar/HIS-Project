package com.medflow.lab.service;

import com.medflow.lab.dto.request.LabOrderNotificationRequest;
import com.medflow.lab.dto.response.LabOrderResponse;
import com.medflow.lab.dto.response.LabOrderWithTestsResponse;
import com.medflow.lab.dto.response.TestDetail;
import com.medflow.lab.exception.InvalidOrderStatusException;
import com.medflow.lab.exception.LabOrderNotFoundException;
import com.medflow.lab.model.ExamType;
import com.medflow.lab.model.LabOrder;
import com.medflow.lab.model.LabResult;
import com.medflow.lab.model.OrderStatus;
import com.medflow.lab.model.Sample;
import com.medflow.lab.repository.ExamTypeRepository;
import com.medflow.lab.repository.LabOrderRepository;
import com.medflow.lab.repository.LabResultRepository;
import com.medflow.lab.repository.SampleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabOrderService {

    private final LabOrderRepository labOrderRepository;
    private final SampleRepository sampleRepository;
    private final LabResultRepository labResultRepository;
    private final ExamTypeRepository examTypeRepository;

    @Transactional
    public LabOrderResponse receiveOrder(LabOrderNotificationRequest request, String userId) {
        log.info("Recibiendo orden de laboratorio: {}", request.getOrderCode());

        LabOrder order = LabOrder.builder()
                .orderCode(request.getOrderCode())
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .appointmentId(request.getAppointmentId())
                .testNames(request.getTestNames())
                .status(OrderStatus.PENDING)
                .orderedAt(LocalDateTime.now())
                .createdBy(userId != null ? userId : request.getDoctorId())
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

    @Transactional
    public LabOrderResponse completeOrderByAppointmentId(String appointmentId, String userId) {
        log.info("Completando orden de laboratorio para cita: {}", appointmentId);

        LabOrder order = labOrderRepository.findFirstByAppointmentIdOrderByOrderedAtDesc(appointmentId)
                .orElseThrow(() -> new LabOrderNotFoundException(
                        "Orden de laboratorio no encontrada para appointmentId: " + appointmentId));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            return mapToResponse(order);
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedBy(userId != null ? userId : "internal");
        return mapToResponse(labOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public LabOrderWithTestsResponse getOrderByAppointmentId(String appointmentId) {
        log.info("Consultando orden por appointmentId: {}", appointmentId);

        LabOrder order = labOrderRepository.findFirstByAppointmentIdOrderByOrderedAtDesc(appointmentId)
                .orElseThrow(() -> new LabOrderNotFoundException(
                        "Orden de laboratorio no encontrada para appointmentId: " + appointmentId));

        // Get all results for this order
        List<LabResult> results = labResultRepository.findByOrderId(order.getId());
        
        // Create a set of test names that have results
        Set<String> testsWithResults = results.stream()
                .map(LabResult::getTestName)
                .collect(Collectors.toSet());

        // Build test details with hasResult flag and exam type information
        List<TestDetail> testDetails = order.getTestNames().stream()
                .map(testName -> {
                    // Try to find exam type by name
                    ExamType examType = examTypeRepository.findFirstByName(testName).orElse(null);
                    
                    return TestDetail.builder()
                            .testName(testName)
                            .testType(examType != null ? examType.getTestType() : "No especificado")
                            .sampleType(examType != null ? examType.getSampleType() : "No especificado")
                            .hasResult(testsWithResults.contains(testName))
                            .build();
                })
                .collect(Collectors.toList());

        return LabOrderWithTestsResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .patientId(order.getPatientId())
                .doctorId(order.getDoctorId())
                .appointmentId(order.getAppointmentId())
                .tests(testDetails)
                .status(order.getStatus())
                .orderedAt(order.getOrderedAt())
                .build();
    }

    private LabOrderResponse mapToResponse(LabOrder order) {
        return LabOrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .patientId(order.getPatientId())
                .doctorId(order.getDoctorId())
                .appointmentId(order.getAppointmentId())
                .testNames(order.getTestNames())
                .status(order.getStatus())
                .orderedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
