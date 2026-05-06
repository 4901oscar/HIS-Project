package com.medflow.lab.service;

import com.medflow.lab.model.ExamType;
import com.medflow.lab.model.ExamTypeStatus;
import com.medflow.lab.repository.ExamTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamTypeService {

    private final ExamTypeRepository repository;

    public List<ExamType> getAll() {
        return repository.findByStatusNot(ExamTypeStatus.DELETED);
    }

    public ExamType create(String code, String name, String description, String testType, String sampleType, ExamTypeStatus status) {
        return repository.save(ExamType.builder()
                .code(code.toUpperCase())
                .name(name)
                .description(description)
                .testType(testType)
                .sampleType(sampleType)
                .status(status != null ? status : ExamTypeStatus.ACTIVE)
                .build());
    }

    public ExamType update(String id, String code, String name, String description, String testType, String sampleType, ExamTypeStatus status) {
        ExamType examType = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado"));
        examType.setCode(code.toUpperCase());
        examType.setName(name);
        examType.setDescription(description);
        examType.setTestType(testType);
        examType.setSampleType(sampleType);
        if (status != null) examType.setStatus(status);
        return repository.save(examType);
    }

    public ExamType toggleActive(String id) {
        ExamType examType = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado"));
        if (examType.getStatus() == ExamTypeStatus.DELETED) {
            throw new IllegalStateException("No se puede cambiar el estado de un examen eliminado");
        }
        ExamTypeStatus next = examType.getStatus() == ExamTypeStatus.ACTIVE
                ? ExamTypeStatus.INACTIVE : ExamTypeStatus.ACTIVE;
        examType.setStatus(next);
        return repository.save(examType);
    }

    public void delete(String id) {
        ExamType examType = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado"));
        examType.setStatus(ExamTypeStatus.DELETED);
        repository.save(examType);
    }
}
