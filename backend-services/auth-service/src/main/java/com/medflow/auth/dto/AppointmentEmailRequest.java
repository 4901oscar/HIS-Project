package com.medflow.auth.dto;

/**
 * DTO for receiving appointment confirmation email requests from clinical-service.
 * Contains all necessary information for generating the confirmation email.
 */
public class AppointmentEmailRequest {
    
    private String toEmail;
    private String firstName;
    private String appointmentDate;
    private String appointmentTime;
    private String doctorName;
    private String invoiceNumber;
    private String qrCodeBase64;
    private String notes;
    private String validFromTime;
    private String validUntilTime;
    
    public AppointmentEmailRequest() {
    }
    
    // Getters and setters
    public String getToEmail() {
        return toEmail;
    }
    
    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getAppointmentDate() {
        return appointmentDate;
    }
    
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    public String getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    
    public String getDoctorName() {
        return doctorName;
    }
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public String getQrCodeBase64() {
        return qrCodeBase64;
    }
    
    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getValidFromTime() {
        return validFromTime;
    }
    
    public void setValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
    }
    
    public String getValidUntilTime() {
        return validUntilTime;
    }
    
    public void setValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
    }
}
