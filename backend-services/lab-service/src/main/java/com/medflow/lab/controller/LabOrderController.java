package com.medflow.lab.controller;

import com.medflow.lab.dto.request.LabOrderNotificationRequest;
import com.medflow.lab.dto.response.LabOrderResponse;
import com.medflow.lab.dto.response.LabOrderWithTestsResponse;
import com.medflow.lab.dto.response.LabResultResponse;
import com.medflow.lab.dto.response.ValidationResponse;
import com.medflow.lab.model.OrderStatus;
import com.medflow.lab.service.LabOrderService;
import com.medflow.lab.service.LabResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lab/orders")
@RequiredArgsConstructor
@Slf4j
public class LabOrderController {

    private final LabOrderService labOrderService;
    private final LabResultService labResultService;

    @PostMapping("/notify")
    public ResponseEntity<LabOrderResponse> notifyOrder(
            @Valid @RequestBody LabOrderNotificationRequest request) {
        log.info("POST /api/lab/orders/notify - orderCode: {}", request.getOrderCode());
        LabOrderResponse response = labOrderService.receiveOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<LabOrderResponse>> getOrders(
            @RequestParam(required = false) OrderStatus status) {
        log.info("GET /api/lab/orders - status: {}", status);
        List<LabOrderResponse> orders = labOrderService.getOrders(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LabOrderResponse> getOrderById(@PathVariable String id) {
        log.info("GET /api/lab/orders/{}", id);
        LabOrderResponse order = labOrderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/by-appointment/{appointmentId}")
    public ResponseEntity<LabOrderWithTestsResponse> getOrderByAppointmentId(
            @PathVariable String appointmentId) {
        log.info("GET /api/lab/orders/by-appointment/{}", appointmentId);
        LabOrderWithTestsResponse order = labOrderService.getOrderByAppointmentId(appointmentId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/collect")
    public ResponseEntity<LabOrderResponse> collectSample(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        log.info("PUT /api/lab/orders/{}/collect - userId: {}", id, userId);
        LabOrderResponse response = labOrderService.collectSample(id, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}/result", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LabOrderResponse> uploadResult(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") String userId) {
        log.info("PUT /api/lab/orders/{}/result - userId: {}", id, userId);
        labResultService.uploadResult(id, file, userId);
        LabOrderResponse response = labOrderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/lab/orders/{orderId}/tests/{testName}/results
     * Uploads a result file for a specific test within an order.
     * Accepts multipart/form-data with file field.
     * 
     * Requirements validated: 6.5, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 12.4
     */
    @PostMapping(value = "/{orderId}/tests/{testName}/results", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LabResultResponse> uploadTestResult(
            @PathVariable String orderId,
            @PathVariable String testName,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") String userId) {
        log.info("POST /api/lab/orders/{}/tests/{}/results - userId: {}", orderId, testName, userId);
        LabResultResponse response = labResultService.uploadTestResult(orderId, testName, file, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/lab/orders/{orderId}/results
     * Retrieves all result files for a specific lab order.
     * Returns a list of LabResultResponse objects with file metadata and download URLs.
     * 
     * Requirements validated: 7.2, 7.3, 12.4
     */
    @GetMapping("/{orderId}/results")
    public ResponseEntity<List<LabResultResponse>> getOrderResults(
            @PathVariable String orderId) {
        log.info("GET /api/lab/orders/{}/results", orderId);
        List<LabResultResponse> results = labResultService.getResultsByOrderId(orderId);
        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/lab/orders/appointment/{appointmentId}/results
     * Retrieves ALL result files for ALL lab orders associated with an appointment.
     * This includes results from multiple orders (e.g., when patient returns to lab for additional tests).
     * 
     * @param appointmentId The appointment ID
     * @return List of all lab results from all orders for this appointment
     */
    @GetMapping("/appointment/{appointmentId}/results")
    public ResponseEntity<List<LabResultResponse>> getAppointmentResults(
            @PathVariable String appointmentId) {
        log.info("GET /api/lab/orders/appointment/{}/results", appointmentId);
        List<LabResultResponse> results = labResultService.getResultsByAppointmentId(appointmentId);
        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/lab/orders/{orderId}/validation/all-tests-complete
     * Validates that all tests in an order have uploaded results.
     * Compares test names to identify missing tests.
     * Returns ValidationResponse with isValid flag and missing tests list.
     * 
     * Requirements validated: 6.8, 6.10, 12.4
     */
    @GetMapping("/{orderId}/validation/all-tests-complete")
    public ResponseEntity<ValidationResponse> validateAllTestsComplete(
            @PathVariable String orderId) {
        log.info("GET /api/lab/orders/{}/validation/all-tests-complete", orderId);
        ValidationResponse validation = labResultService.validateAllTestsComplete(orderId);
        return ResponseEntity.ok(validation);
    }

    /**
     * GET /api/lab/orders/results/{resultId}/download
     * Downloads a lab result file by result ID.
     * Returns the file as application/pdf or image/* depending on file type.
     */
    @GetMapping("/results/{resultId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResult(
            @PathVariable String resultId) {
        log.info("GET /api/lab/orders/results/{}/download", resultId);
        return labResultService.downloadResult(resultId);
    }
}
