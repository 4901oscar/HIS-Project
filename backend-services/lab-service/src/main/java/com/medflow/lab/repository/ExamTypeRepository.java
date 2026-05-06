package com.medflow.lab.repository;

import com.medflow.lab.model.ExamType;
import com.medflow.lab.model.ExamTypeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamTypeRepository extends JpaRepository<ExamType, String> {
    List<ExamType> findByStatusNot(ExamTypeStatus status);
    boolean existsByCode(String code);
    java.util.Optional<ExamType> findByName(String name);
}
