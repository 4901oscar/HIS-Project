package com.medflow.lab.repository;

import com.medflow.lab.model.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamTypeRepository extends JpaRepository<ExamType, String> {
    List<ExamType> findByActiveTrue();
    boolean existsByCode(String code);
}
