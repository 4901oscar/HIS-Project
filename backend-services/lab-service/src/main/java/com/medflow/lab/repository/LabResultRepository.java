package com.medflow.lab.repository;

import com.medflow.lab.model.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, String> {

    List<LabResult> findByPatientIdOrderByUploadedAtDesc(String patientId);
}
