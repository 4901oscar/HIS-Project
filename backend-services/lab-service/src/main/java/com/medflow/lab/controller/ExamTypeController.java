package com.medflow.lab.controller;

import com.medflow.lab.model.ExamType;
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(examTypeService.create(req.getCode(), req.getName(), req.getDescription()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamType> update(@PathVariable String id, @RequestBody ExamTypeRequest req) {
        return ResponseEntity.ok(examTypeService.update(id, req.getCode(), req.getName(), req.getDescription()));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ExamType> toggle(@PathVariable String id) {
        return ResponseEntity.ok(examTypeService.toggleActive(id));
    }

    @Data
    static class ExamTypeRequest {
        private String code;
        private String name;
        private String description;
    }
}
