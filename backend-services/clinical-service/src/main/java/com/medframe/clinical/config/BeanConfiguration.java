package com.medframe.clinical.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medframe.clinical.domain.port.out.*;
import com.medframe.clinical.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public class BeanConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }

    // Domain Services Configuration
    // These are pure Java classes without Spring annotations,
    // configured as beans here following hexagonal architecture principles

    @Bean
    public TriageEngine triageEngine(VitalSignsRepository vitalSignsRepository,
                                     ManchesterCatalogRepository manchesterCatalogRepository) {
        return new TriageEngine(vitalSignsRepository, manchesterCatalogRepository);
    }

    @Bean
    public VitalSignsRecorder vitalSignsRecorder(VitalSignsRepository vitalSignsRepository) {
        return new VitalSignsRecorder(vitalSignsRepository);
    }

    @Bean
    public AppointmentManager appointmentManager(AppointmentRepository appointmentRepository,
                                                  AppointmentSlotCache slotCache,
                                                  PatientServiceClient patientServiceClient) {
        return new AppointmentManager(appointmentRepository, slotCache, patientServiceClient);
    }

    @Bean
    public ConsultationManager consultationManager(ConsultationRepository consultationRepository,
                                                    AppointmentRepository appointmentRepository) {
        return new ConsultationManager(consultationRepository, appointmentRepository);
    }

    @Bean
    public PrescriptionGenerator prescriptionGenerator(PrescriptionRepository prescriptionRepository,
                                                        PharmacyServiceClient pharmacyServiceClient,
                                                        SecureRandom secureRandom) {
        return new PrescriptionGenerator(prescriptionRepository, pharmacyServiceClient, secureRandom);
    }

    @Bean
    public LabOrderGenerator labOrderGenerator(LabOrderRepository labOrderRepository,
                                                LabServiceClient labServiceClient,
                                                SecureRandom secureRandom) {
        return new LabOrderGenerator(labOrderRepository, labServiceClient, secureRandom);
    }

    @Bean
    public MedicalHistoryAggregator medicalHistoryAggregator(PatientServiceClient patientServiceClient,
                                                              ConsultationRepository consultationRepository,
                                                              VitalSignsRepository vitalSignsRepository,
                                                              PrescriptionRepository prescriptionRepository,
                                                              LabOrderRepository labOrderRepository) {
        return new MedicalHistoryAggregator(patientServiceClient, consultationRepository,
                vitalSignsRepository, prescriptionRepository, labOrderRepository);
    }
}
