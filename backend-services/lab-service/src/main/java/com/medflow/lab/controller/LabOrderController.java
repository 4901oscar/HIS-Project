package com.medflow.lab.controller;

import com.medflow.lab.dto.request.LabOrderNotificationRequest;
import com.medflow.lab.dto.response.LabOrderResponse;
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
}
