package com.medflow.lab.repository;

import com.medflow.lab.model.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleRepository extends JpaRepository<Sample, String> {

    List<Sample> findByOrderId(String orderId);
}
