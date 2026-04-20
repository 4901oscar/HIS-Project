package com.medflow.lab.service;

import com.medflow.lab.model.ExamType;
import com.medflow.lab.repository.ExamTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamTypeService {

    private final ExamTypeRepository repository;

    public List<ExamType> getAll() {
        return repository.findAll();
    }

    public ExamType create(String code, String name, String description) {
        ExamType examType = ExamType.builder()
                .code(code.toUpperCase())
                .name(name)
                .description(description)
                .active(true)
                .build();
        return repository.save(examType);
    }

    public ExamType update(String id, String code, String name, String description) {
        ExamType examType = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado"));
        examType.setCode(code.toUpperCase());
        examType.setName(name);
        examType.setDescription(description);
        return repository.save(examType);
    }

    public ExamType toggleActive(String id) {
        ExamType examType = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado"));
        examType.setActive(!examType.isActive());
        return repository.save(examType);
    }
}
