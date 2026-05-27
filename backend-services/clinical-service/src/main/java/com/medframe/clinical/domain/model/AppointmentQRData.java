package com.medframe.clinical.domain.model;

import java.time.ZonedDateTime;

/**
 * Value object representing the data encoded in an appointment QR code.
 * Contains appointment identification and scheduling information in ISO 8601 format.
 */
public class AppointmentQRData {
    
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String date;        // ISO 8601: YYYY-MM-DD
    private String time;        // ISO 8601: HH:mm:ss
    private String generatedAt; // ISO 8601 with timezone
    
    public AppointmentQRData() {
    }
    
    public AppointmentQRData(String appointmentId, String patientId, String doctorId,
                            String date, String time, String generatedAt) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.generatedAt = generatedAt;
    }
    
    /**
     * Factory method to create AppointmentQRData from an Appointment entity.
     * @param appointment The appointment to convert
     * @return AppointmentQRData with ISO 8601 formatted dates and times
     */
    public static AppointmentQRData fromAppointment(Appointment appointment) {
        return new AppointmentQRData(
            appointment.getId(),
            appointment.getPatientId(),
            appointment.getDoctorId(),
            appointment.getAppointmentDate().toString(),
            appointment.getAppointmentTime().toString(),
            ZonedDateTime.now().toString()
        );
    }
    
    // Getters and setters
    public String getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    
    public String getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        AppointmentQRData that = (AppointmentQRData) o;
        
        if (appointmentId != null ? !appointmentId.equals(that.appointmentId) : that.appointmentId != null)
            return false;
        if (patientId != null ? !patientId.equals(that.patientId) : that.patientId != null)
            return false;
        if (doctorId != null ? !doctorId.equals(that.doctorId) : that.doctorId != null)
            return false;
        if (date != null ? !date.equals(that.date) : that.date != null)
            return false;
        if (time != null ? !time.equals(that.time) : that.time != null)
            return false;
        return generatedAt != null ? generatedAt.equals(that.generatedAt) : that.generatedAt == null;
    }
    
    @Override
    public int hashCode() {
        int result = appointmentId != null ? appointmentId.hashCode() : 0;
        result = 31 * result + (patientId != null ? patientId.hashCode() : 0);
        result = 31 * result + (doctorId != null ? doctorId.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (generatedAt != null ? generatedAt.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "AppointmentQRData{" +
                "appointmentId='" + appointmentId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", doctorId='" + doctorId + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", generatedAt='" + generatedAt + '\'' +
                '}';
    }
}
