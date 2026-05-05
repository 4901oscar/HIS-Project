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
    public ResponseEntity<ExamType> create(@RequestBody ExamTypeRequest req) {
        ExamTypeStatus status = parseStatus(req.getStatus(), ExamTypeStatus.ACTIVE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(examTypeService.create(req.getCode(), req.getName(), req.getDescription(), status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamType> update(@PathVariable String id, @RequestBody ExamTypeRequest req) {
        ExamTypeStatus status = parseStatus(req.getStatus(), null);
        return ResponseEntity.ok(examTypeService.update(id, req.getCode(), req.getName(), req.getDescription(), status));
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
        private String status;
    }
}
