package com.medflow.lab.controller;

import com.medflow.lab.dto.response.LabResultResponse;
import com.medflow.lab.service.LabResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lab/results")
@RequiredArgsConstructor
@Slf4j
public class LabResultController {

    private final LabResultService labResultService;

    @GetMapping("/{patientId}")
    public ResponseEntity<List<LabResultResponse>> getPatientResults(
            @PathVariable String patientId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole) {
        log.info("GET /api/lab/results/{} - userId: {}, role: {}", patientId, userId, userRole);
        List<LabResultResponse> results = labResultService.getPatientResults(patientId, userId, userRole);
        return ResponseEntity.ok(results);
    }
}
