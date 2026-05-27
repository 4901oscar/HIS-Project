package com.medframe.clinical.config;

import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.port.out.AppointmentSlotCache;
import com.medframe.clinical.domain.port.out.DoctorAvailabilityRepository;
import com.medframe.clinical.domain.port.out.DoctorRepository;
import com.medframe.clinical.domain.service.CapacityController;
import com.medframe.clinical.domain.service.DoctorAssignmentService;
import com.medframe.clinical.domain.service.ShiftManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for domain services.
 * 
 * <p>This configuration class registers domain services as Spring beans
 * while maintaining hexagonal architecture principles. Domain services
 * remain free of Spring annotations, and dependency injection is configured
 * here in the infrastructure layer.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Configuration
public class DomainConfig {
    
    /**
     * Registers ShiftManager as a Spring bean.
     * 
     * @param doctorRepository repository for doctor persistence
     * @param doctorAvailabilityRepository repository for availability persistence
     * @return configured ShiftManager instance
     */
    @Bean
    public ShiftManager shiftManager(
            DoctorRepository doctorRepository,
            DoctorAvailabilityRepository doctorAvailabilityRepository) {
        return new ShiftManager(doctorRepository, doctorAvailabilityRepository);
    }
    
    /**
     * Registers DoctorAssignmentService as a Spring bean.
     * 
     * @param doctorRepository repository for doctor persistence
     * @param doctorAvailabilityRepository repository for availability persistence
     * @param appointmentRepository repository for appointment persistence
     * @return configured DoctorAssignmentService instance
     */
    @Bean
    public DoctorAssignmentService doctorAssignmentService(
            DoctorRepository doctorRepository,
            DoctorAvailabilityRepository doctorAvailabilityRepository,
            AppointmentRepository appointmentRepository) {
        return new DoctorAssignmentService(
                doctorRepository,
                doctorAvailabilityRepository,
                appointmentRepository
        );
    }
    
    /**
     * Registers CapacityController as a Spring bean.
     * 
     * @param doctorRepository repository for doctor persistence
     * @param doctorAvailabilityRepository repository for availability persistence
     * @param appointmentSlotCache cache for appointment slot management
     * @return configured CapacityController instance
     */
    @Bean
    public CapacityController capacityController(
            DoctorRepository doctorRepository,
            DoctorAvailabilityRepository doctorAvailabilityRepository,
            AppointmentSlotCache appointmentSlotCache) {
        return new CapacityController(
                doctorRepository,
                doctorAvailabilityRepository,
                appointmentSlotCache
        );
    }
}
