package com.medflow.lab.service;

import com.medflow.lab.dto.response.LabResultResponse;
import com.medflow.lab.dto.response.ValidationResponse;
import com.medflow.lab.exception.FileSizeExceededException;
import com.medflow.lab.exception.InvalidFileFormatException;
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
import java.util.Arrays;
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

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;

    @Value("#{'${file.upload.allowed-types}'.split(',')}")
    private List<String> allowedFileTypes;

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
            throw new IllegalArgumentException("El archivo es obligatorio");
        }

        // Validate file format and size
        validateFileFormat(file);
        validateFileSize(file);

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

    /**
     * Uploads a result file for a specific test within an order.
     * This method allows uploading results for individual tests without changing the order status.
     * 
     * @param orderId The ID of the lab order
     * @param testName The name of the test
     * @param file The result file to upload
     * @param uploadedBy The ID of the user uploading the file
     * @return LabResultResponse with the uploaded result metadata
     * 
     * Requirements validated: 6.5, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 12.4
     */
    @Transactional
    public LabResultResponse uploadTestResult(String orderId, String testName, MultipartFile file, String uploadedBy) {
        log.info("Subiendo resultado para orden: {}, test: {}", orderId, testName);

        // Validate order exists
        LabOrder order = labOrderRepository.findById(orderId)
                .orElseThrow(() -> new LabOrderNotFoundException(
                        "Orden de laboratorio no encontrada con ID: " + orderId));

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo es obligatorio");
        }

        // Validate file format and size
        validateFileFormat(file);
        validateFileSize(file);

        // Generate unique filename using FileStorageService pattern
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".pdf";
        String uniqueFilename = orderId + "_" + testName + "_" + UUID.randomUUID() + extension;

        // Save file to filesystem
        String filePath = saveFileWithName(file, uniqueFilename);

        // Create result record with test-specific metadata
        LabResult result = LabResult.builder()
                .orderId(orderId)
                .testName(testName)
                .patientId(order.getPatientId())
                .resultFilePath(filePath)
                .originalFilename(originalFilename)
                .fileSize(file.getSize())
                .uploadedBy(uploadedBy)
                .uploadedAt(LocalDateTime.now())
                .build();
        LabResult savedResult = labResultRepository.save(result);

        log.info("Resultado guardado con ID: {} para test: {}", savedResult.getId(), testName);

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

    /**
     * Retrieves all result files for a specific lab order.
     * Returns a list of LabResultResponse objects with file metadata and download URLs.
     * 
     * @param orderId The ID of the lab order
     * @return List of LabResultResponse objects
     * 
     * Requirements validated: 7.2, 7.3, 12.4
     */
    @Transactional(readOnly = true)
    public List<LabResultResponse> getResultsByOrderId(String orderId) {
        log.info("Consultando resultados para orden: {}", orderId);

        // Validate order exists
        labOrderRepository.findById(orderId)
                .orElseThrow(() -> new LabOrderNotFoundException(
                        "Orden de laboratorio no encontrada con ID: " + orderId));

        List<LabResult> results = labResultRepository.findByOrderId(orderId);

        return results.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all result files for ALL lab orders associated with an appointment.
     * This includes results from multiple orders (e.g., when patient returns to lab for additional tests).
     * 
     * @param appointmentId The ID of the appointment
     * @return List of LabResultResponse objects from all orders for this appointment
     */
    @Transactional(readOnly = true)
    public List<LabResultResponse> getResultsByAppointmentId(String appointmentId) {
        log.info("Consultando todos los resultados para appointmentId: {}", appointmentId);

        // Get all lab orders for this appointment (ordered by most recent first)
        List<LabOrder> orders = labOrderRepository.findByAppointmentIdOrderByOrderedAtDesc(appointmentId);

        if (orders.isEmpty()) {
            log.info("No se encontraron órdenes para appointmentId: {}", appointmentId);
            return List.of();
        }

        // Get all results from all orders
        List<LabResult> allResults = orders.stream()
                .flatMap(order -> labResultRepository.findByOrderId(order.getId()).stream())
                .collect(Collectors.toList());

        log.info("Se encontraron {} resultados de {} órdenes para appointmentId: {}", 
                allResults.size(), orders.size(), appointmentId);

        return allResults.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validates that all tests in a lab order have uploaded results.
     * Compares the test names in the order against the test names in the uploaded results.
     * 
     * @param orderId The ID of the lab order to validate
     * @return ValidationResponse with isValid flag and list of missing tests
     * 
     * Requirements validated: 6.8, 6.10, 12.4
     */
    @Transactional(readOnly = true)
    public ValidationResponse validateAllTestsComplete(String orderId) {
        log.info("Validando completitud de resultados para orden: {}", orderId);

        // 1. Get lab order
        LabOrder order = labOrderRepository.findById(orderId)
                .orElseThrow(() -> new LabOrderNotFoundException(
                        "Orden de laboratorio no encontrada con ID: " + orderId));

        // 2. Get all results for this order
        List<LabResult> results = labResultRepository.findByOrderId(orderId);

        // 3. Extract test names from results
        java.util.Set<String> testsWithResults = results.stream()
                .map(LabResult::getTestName)
                .collect(Collectors.toSet());

        // 4. Find missing tests
        List<String> missingTests = order.getTestNames().stream()
                .filter(testName -> !testsWithResults.contains(testName))
                .collect(Collectors.toList());

        // 5. Build validation response
        boolean isValid = missingTests.isEmpty();
        String message = isValid 
                ? "Todos los exámenes tienen resultados cargados"
                : String.format("Faltan resultados para %d examen(es): %s", 
                        missingTests.size(), 
                        String.join(", ", missingTests));

        log.info("Validación completada - isValid: {}, missingTests: {}", isValid, missingTests.size());

        return ValidationResponse.builder()
                .isValid(isValid)
                .missingTests(missingTests)
                .message(message)
                .build();
    }

    /**
     * Validates that the uploaded file has an allowed format.
     * Accepts only PDF, JPEG, and PNG files.
     * 
     * @param file The multipart file to validate
     * @throws InvalidFileFormatException if the file format is not allowed
     * 
     * Requirements validated: 6.4, 11.1, 13.2
     */
    private void validateFileFormat(MultipartFile file) {
        String contentType = file.getContentType();
        
        if (contentType == null || !allowedFileTypes.contains(contentType)) {
            String allowedFormats = String.join(", ", Arrays.asList("PDF", "JPEG", "PNG"));
            throw new InvalidFileFormatException(
                    String.format("Formato de archivo no permitido: %s. Solo se aceptan archivos %s.", 
                            contentType != null ? contentType : "desconocido", 
                            allowedFormats));
        }
        
        log.debug("Formato de archivo validado: {}", contentType);
    }

    /**
     * Validates that the uploaded file does not exceed the maximum allowed size.
     * Maximum size is 10 MB (10485760 bytes).
     * 
     * @param file The multipart file to validate
     * @throws FileSizeExceededException if the file size exceeds the limit
     * 
     * Requirements validated: 11.2, 13.2
     */
    private void validateFileSize(MultipartFile file) {
        long fileSize = file.getSize();
        
        if (fileSize > maxFileSize) {
            double fileSizeMB = fileSize / (1024.0 * 1024.0);
            double maxSizeMB = maxFileSize / (1024.0 * 1024.0);
            throw new FileSizeExceededException(
                    String.format("El archivo excede el tamaño máximo permitido. Tamaño del archivo: %.2f MB, Máximo permitido: %.2f MB.", 
                            fileSizeMB, 
                            maxSizeMB));
        }
        
        log.debug("Tamaño de archivo validado: {} bytes", fileSize);
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

    /**
     * Saves a file with a specific filename.
     * 
     * @param file The multipart file to save
     * @param filename The filename to use
     * @return The absolute path where the file was saved
     */
    private String saveFileWithName(MultipartFile file, String filename) {
        try {
            // Create storage directory if it doesn't exist
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }

            Path filePath = storageDir.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Archivo guardado en: {}", filePath);
            return filePath.toString();

        } catch (IOException e) {
            log.error("Error al guardar archivo: {}", e.getMessage());
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

    private LabResultResponse mapToResponse(LabResult result) {
        return LabResultResponse.builder()
                .id(result.getId())
                .orderId(result.getOrderId())
                .testName(result.getTestName())
                .originalFilename(result.getOriginalFilename())
                .fileSize(result.getFileSize())
                .uploadedAt(result.getUploadedAt())
                .uploadedBy(result.getUploadedBy())
                .downloadUrl(result.getResultFilePath())
                .build();
    }

    /**
     * Downloads a lab result file by result ID.
     * 
     * @param resultId The lab result ID
     * @return ResponseEntity with the file resource
     */
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadResult(String resultId) {
        log.info("Descargando resultado con ID: {}", resultId);

        // Get result from database
        LabResult result = labResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Resultado no encontrado con ID: " + resultId));

        try {
            // Load file as Resource
            java.nio.file.Path filePath = java.nio.file.Paths.get(result.getResultFilePath());
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("No se puede leer el archivo: " + result.getResultFilePath());
            }

            // Determine content type
            String contentType = "application/octet-stream";
            if (result.getOriginalFilename() != null) {
                if (result.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (result.getOriginalFilename().toLowerCase().endsWith(".jpg") || 
                           result.getOriginalFilename().toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (result.getOriginalFilename().toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                }
            }

            return org.springframework.http.ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"" + result.getOriginalFilename() + "\"")
                    .body(resource);

        } catch (java.net.MalformedURLException e) {
            log.error("Error al cargar archivo: {}", e.getMessage());
            throw new RuntimeException("Error al cargar el archivo", e);
        }
    }
}
