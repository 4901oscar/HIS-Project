package com.medflow.lab.controller;

import com.medflow.lab.model.ExamType;
import com.medflow.lab.model.ExamTypeStatus;
import com.medflow.lab.service.ExamTypeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lab/exam-types")
@RequiredArgsConstructor
public class ExamTypeController {

    private final ExamTypeService examTypeService;

    @GetMapping
    public ResponseEntity<List<ExamType>> getAll() {
        return ResponseEntity.ok(examTypeService.getAll());
    }

    @PostMapping
    public ResponseEntity<ExamType> create(
            @RequestBody ExamTypeRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        ExamTypeStatus status = parseStatus(req.getStatus(), ExamTypeStatus.ACTIVE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(examTypeService.create(req.getCode(), req.getName(), req.getDescription(),
                        req.getTestType(), req.getSampleType(), status, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamType> update(
            @PathVariable String id,
            @RequestBody ExamTypeRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        ExamTypeStatus status = parseStatus(req.getStatus(), null);
        return ResponseEntity.ok(examTypeService.update(id, req.getCode(), req.getName(), req.getDescription(),
                req.getTestType(), req.getSampleType(), status, userId));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ExamType> toggleActive(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(examTypeService.toggleActive(id, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        examTypeService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    private ExamTypeStatus parseStatus(String value, ExamTypeStatus fallback) {
        if (value == null) return fallback;
        try { return ExamTypeStatus.valueOf(value); }
        catch (IllegalArgumentException e) { return fallback; }
    }

    @Data
    static class ExamTypeRequest {
        private String code;
        private String name;
        private String description;
        private String testType;
        private String sampleType;
        private String status;
    }
}
