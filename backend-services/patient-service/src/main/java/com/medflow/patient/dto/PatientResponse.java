package com.medflow.patient.dto;

import com.medflow.patient.model.Patient;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientResponse {
    private String id;
    private String dpi;
    private String nit;
    private String fullName;
    private String firstName;
    private String firstLastName;
    private LocalDate birthDate;
    private String gender;
    private String email;
    private String phone;
    private String department;
    private String municipality;
    private String zone;
    private String address;
    private boolean active;

    public static PatientResponse from(Patient p) {
        PatientResponse r = new PatientResponse();
        r.setId(p.getId().toString());
        r.setDpi(p.getDpi());
        r.setNit(p.getNit());
        r.setFullName(p.getFullName());
        r.setFirstName(p.getFirstName());
        r.setFirstLastName(p.getFirstLastName());
        r.setBirthDate(p.getBirthDate());
        r.setGender(p.getGender().name());
        r.setEmail(p.getEmail());
        r.setPhone(p.getPhone());
        r.setDepartment(p.getDepartment());
        r.setMunicipality(p.getMunicipality());
        r.setZone(p.getZone());
        r.setAddress(p.getAddress());
        r.setActive(p.isActive());
        return r;
    }
}
