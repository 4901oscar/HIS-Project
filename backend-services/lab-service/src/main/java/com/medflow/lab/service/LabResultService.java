package com.medflow.lab.service;

import com.medflow.lab.dto.response.LabResultResponse;
import com.medflow.lab.exception.InvalidOrderStatusException;
import com.medflow.lab.exception.LabOrderNotFoundException;
import com.medflow.lab.exception.UnauthorizedException;
import com.medflow.lab.model.LabOrder;
import com.medflow.lab.model.LabResult;
import com.medflow.lab.model.OrderStatus;
import com.medflow.lab.repository.LabOrderRepository;
import com.medflow.lab.repository.LabResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabResultService {

    private final LabResultRepository labResultRepository;
    private final LabOrderRepository labOrderRepository;

    @Value("${lab.results.storage-path}")
    private String storagePath;

    @Transactional
    public LabResultResponse uploadResult(String orderId, MultipartFile file, String uploadedBy) {
        log.info("Subiendo resultado para orden: {}", orderId);

        // Validate order exists
        LabOrder order = labOrderRepository.findById(orderId)
                .orElseThrow(() -> new LabOrderNotFoundException(
                        "Orden de laboratorio no encontrada con ID: " + orderId));

        // Validate status transition: IN_PROGRESS → COMPLETED
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new InvalidOrderStatusException(
                    "Solo las órdenes con estado IN_PROGRESS pueden pasar a COMPLETED. Estado actual: " + order.getStatus());
        }

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo PDF es obligatorio");
        }

        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Solo se permiten archivos PDF");
        }

        // Save file to filesystem
        String filePath = saveFile(file, orderId);

        // Create result record
        LabResult result = LabResult.builder()
                .orderId(orderId)
                .patientId(order.getPatientId())
                .resultFilePath(filePath)
                .uploadedBy(uploadedBy)
                .uploadedAt(LocalDateTime.now())
                .build();
        LabResult savedResult = labResultRepository.save(result);

        // Update order status to COMPLETED
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        labOrderRepository.save(order);

        log.info("Resultado guardado con ID: {}", savedResult.getId());

        return mapToResponse(savedResult);
    }

    @Transactional(readOnly = true)
    public List<LabResultResponse> getPatientResults(String patientId, String requestingUserId, String userRole) {
        log.info("Consultando resultados para paciente: {}", patientId);

        // Validate PATIENT role can only access own results
        if ("PATIENT".equals(userRole) && !patientId.equals(requestingUserId)) {
            throw new UnauthorizedException(
                    "Los pacientes solo pueden ver sus propios resultados");
        }

        List<LabResult> results = labResultRepository.findByPatientIdOrderByUploadedAtDesc(patientId);

        return results.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String saveFile(MultipartFile file, String orderId) {
        try {
            // Create storage directory if it doesn't exist
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".pdf";
            String filename = orderId + "_" + UUID.randomUUID() + extension;
            Path filePath = storageDir.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Archivo guardado en: {}", filePath);
            return filePath.toString();

        } catch (IOException e) {
            log.error("Error al guardar archivo: {}", e.getMessage());
            throw new RuntimeException("Error al guardar el archivo PDF", e);
        }
    }

    private LabResultResponse mapToResponse(LabResult result) {
        return LabResultResponse.builder()
                .id(result.getId())
                .orderId(result.getOrderId())
                .patientId(result.getPatientId())
                .resultFilePath(result.getResultFilePath())
                .uploadedAt(result.getUploadedAt())
                .uploadedBy(result.getUploadedBy())
                .build();
    }
}
