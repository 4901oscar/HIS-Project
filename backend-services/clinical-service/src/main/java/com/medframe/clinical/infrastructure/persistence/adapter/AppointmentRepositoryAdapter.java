package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.infrastructure.persistence.repository.JpaAppointmentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements the AppointmentRepository output port using JPA.
 */
@Component
public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final JpaAppointmentRepository jpaRepository;
    private final AppointmentMapper mapper;

    public AppointmentRepositoryAdapter(JpaAppointmentRepository jpaRepository, AppointmentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Appointment save(Appointment appointment) {
        var entity = mapper.toEntity(appointment);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Appointment> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Appointment> findByDoctorIdAndDate(String doctorId, LocalDate date) {
        return jpaRepository.findByDoctorIdAndAppointmentDate(doctorId, date).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByPatientId(String patientId) {
        return jpaRepository.findByPatientId(patientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        return jpaRepository.findByAppointmentDate(date).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByDoctorId(String doctorId) {
        return jpaRepository.findByDoctorId(doctorId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Appointment> findPendingTriage() {
        return jpaRepository.findPendingTriage().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Appointment> findAppointmentsWithoutInvoice() {
        return jpaRepository.findByInvoiceIdIsNull().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Appointment update(Appointment appointment) {
        var entity = mapper.toEntity(appointment);
        var updated = jpaRepository.save(entity);
        return mapper.toDomain(updated);
    }
}
