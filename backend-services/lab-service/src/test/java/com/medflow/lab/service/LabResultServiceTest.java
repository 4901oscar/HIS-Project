package com.medflow.lab.service;

import com.medflow.lab.dto.response.LabResultResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabResultServiceTest {

    @Mock
    private LabResultRepository labResultRepository;

    @Mock
    private LabOrderRepository labOrderRepository;

    @InjectMocks
    private LabResultService labResultService;

    @TempDir
    Path tempDir;

    private LabOrder testOrder;
    private LabResult testResult;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testOrder = LabOrder.builder()
                .id("order-123")
                .orderCode("AB12CD34")
                .patientId("patient-1")
                .doctorId("doctor-1")
                .testNames(List.of("Hemograma"))
                .status(OrderStatus.IN_PROGRESS)
                .orderedAt(LocalDateTime.now())
                .build();

        testResult = LabResult.builder()
                .id("result-123")
                .orderId("order-123")
                .patientId("patient-1")
                .resultFilePath("/path/to/result.pdf")
                .uploadedAt(LocalDateTime.now())
                .uploadedBy("tech-1")
                .build();

        testFile = new MockMultipartFile(
                "file",
                "result.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        ReflectionTestUtils.setField(labResultService, "storagePath", tempDir.toString());
        ReflectionTestUtils.setField(labResultService, "maxFileSize", 10485760L); // 10 MB
        ReflectionTestUtils.setField(labResultService, "allowedFileTypes", 
                List.of("application/pdf", "image/jpeg", "image/png"));
    }

    @Test
    void uploadResult_WithInProgressStatus_ShouldCompleteOrder() {
        // Arrange
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));
        when(labResultRepository.save(any(LabResult.class))).thenReturn(testResult);
        when(labOrderRepository.save(any(LabOrder.class))).thenReturn(testOrder);

        // Act
        LabResultResponse response = labResultService.uploadResult("order-123", testFile, "tech-1");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo("order-123");
        verify(labOrderRepository, times(1)).save(argThat(order -> 
                order.getStatus() == OrderStatus.COMPLETED
        ));
        verify(labResultRepository, times(1)).save(any(LabResult.class));
    }

    @Test
    void uploadResult_WithPendingStatus_ShouldThrowException() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> labResultService.uploadResult("order-123", testFile, "tech-1"))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("Solo las órdenes con estado IN_PROGRESS pueden pasar a COMPLETED");
    }

    @Test
    void uploadResult_WithCompletedStatus_ShouldThrowException() {
        // Arrange
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> labResultService.uploadResult("order-123", testFile, "tech-1"))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("Solo las órdenes con estado IN_PROGRESS pueden pasar a COMPLETED");
    }

    @Test
    void uploadResult_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(labOrderRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> labResultService.uploadResult("invalid-id", testFile, "tech-1"))
                .isInstanceOf(LabOrderNotFoundException.class)
                .hasMessageContaining("Orden de laboratorio no encontrada");
    }

    @Test
    void uploadResult_WithNullFile_ShouldThrowException() {
        // Arrange
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> labResultService.uploadResult("order-123", null, "tech-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El archivo es obligatorio");
    }

    @Test
    void uploadResult_WithEmptyFile_ShouldThrowException() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile("file", "result.pdf", "application/pdf", new byte[0]);
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> labResultService.uploadResult("order-123", emptyFile, "tech-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El archivo es obligatorio");
    }

    @Test
    void uploadResult_WithNonPdfFile_ShouldThrowException() {
        // Arrange
        MultipartFile nonPdfFile = new MockMultipartFile(
                "file", "result.txt", "text/plain", "Text content".getBytes()
        );
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> labResultService.uploadResult("order-123", nonPdfFile, "tech-1"))
                .isInstanceOf(InvalidFileFormatException.class)
                .hasMessageContaining("Formato de archivo no permitido");
    }

    @Test
    void uploadResult_WithJpegFile_ShouldSucceed() {
        // Arrange
        MultipartFile jpegFile = new MockMultipartFile(
                "file", "result.jpg", "image/jpeg", "JPEG content".getBytes()
        );
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));
        when(labResultRepository.save(any(LabResult.class))).thenReturn(testResult);
        when(labOrderRepository.save(any(LabOrder.class))).thenReturn(testOrder);

        // Act
        LabResultResponse response = labResultService.uploadResult("order-123", jpegFile, "tech-1");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo("order-123");
    }

    @Test
    void uploadResult_WithPngFile_ShouldSucceed() {
        // Arrange
        MultipartFile pngFile = new MockMultipartFile(
                "file", "result.png", "image/png", "PNG content".getBytes()
        );
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));
        when(labResultRepository.save(any(LabResult.class))).thenReturn(testResult);
        when(labOrderRepository.save(any(LabOrder.class))).thenReturn(testOrder);

        // Act
        LabResultResponse response = labResultService.uploadResult("order-123", pngFile, "tech-1");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo("order-123");
    }

    @Test
    void uploadResult_WithFileSizeExceeded_ShouldThrowException() {
        // Arrange
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11 MB
        MultipartFile largeFile = new MockMultipartFile(
                "file", "result.pdf", "application/pdf", largeContent
        );
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> labResultService.uploadResult("order-123", largeFile, "tech-1"))
                .isInstanceOf(FileSizeExceededException.class)
                .hasMessageContaining("El archivo excede el tamaño máximo permitido");
    }

    @Test
    void getPatientResults_AsDoctor_ShouldReturnResults() {
        // Arrange
        when(labResultRepository.findByPatientIdOrderByUploadedAtDesc("patient-1"))
                .thenReturn(List.of(testResult));

        // Act
        List<LabResultResponse> results = labResultService.getPatientResults(
                "patient-1", "doctor-1", "DOCTOR"
        );

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOrderId()).isEqualTo("order-123");
        verify(labResultRepository, times(1)).findByPatientIdOrderByUploadedAtDesc("patient-1");
    }

    @Test
    void getPatientResults_AsPatientOwnResults_ShouldReturnResults() {
        // Arrange
        when(labResultRepository.findByPatientIdOrderByUploadedAtDesc("patient-1"))
                .thenReturn(List.of(testResult));

        // Act
        List<LabResultResponse> results = labResultService.getPatientResults(
                "patient-1", "patient-1", "PATIENT"
        );

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOrderId()).isEqualTo("order-123");
    }

    @Test
    void getPatientResults_AsPatientOtherResults_ShouldThrowUnauthorized() {
        // Act & Assert
        assertThatThrownBy(() -> labResultService.getPatientResults(
                "patient-2", "patient-1", "PATIENT"
        ))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Los pacientes solo pueden ver sus propios resultados");
    }

    @Test
    void getPatientResults_ShouldReturnInDescendingOrder() {
        // Arrange
        LabResult result1 = LabResult.builder()
                .id("result-1")
                .orderId("order-1")
                .patientId("patient-1")
                .resultFilePath("/path/1.pdf")
                .uploadedAt(LocalDateTime.now().minusDays(2))
                .uploadedBy("tech-1")
                .build();

        LabResult result2 = LabResult.builder()
                .id("result-2")
                .orderId("order-2")
                .patientId("patient-1")
                .resultFilePath("/path/2.pdf")
                .uploadedAt(LocalDateTime.now().minusDays(1))
                .uploadedBy("tech-1")
                .build();

        when(labResultRepository.findByPatientIdOrderByUploadedAtDesc("patient-1"))
                .thenReturn(List.of(result2, result1)); // Already sorted by repository

        // Act
        List<LabResultResponse> results = labResultService.getPatientResults(
                "patient-1", "doctor-1", "DOCTOR"
        );

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getUploadedAt()).isAfter(results.get(1).getUploadedAt());
    }
}
