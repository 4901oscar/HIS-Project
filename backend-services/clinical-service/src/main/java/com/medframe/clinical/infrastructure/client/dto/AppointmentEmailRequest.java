package com.medframe.clinical.infrastructure.client.dto;

/**
 * DTO for sending appointment confirmation email requests to auth-service.
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
    
    private AppointmentEmailRequest(Builder builder) {
        this.toEmail = builder.toEmail;
        this.firstName = builder.firstName;
        this.appointmentDate = builder.appointmentDate;
        this.appointmentTime = builder.appointmentTime;
        this.doctorName = builder.doctorName;
        this.invoiceNumber = builder.invoiceNumber;
        this.qrCodeBase64 = builder.qrCodeBase64;
        this.notes = builder.notes;
        this.validFromTime = builder.validFromTime;
        this.validUntilTime = builder.validUntilTime;
    }
    
    public static Builder builder() {
        return new Builder();
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
    
    // Builder pattern
    public static class Builder {
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
        
        public Builder toEmail(String toEmail) {
            this.toEmail = toEmail;
            return this;
        }
        
        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public Builder appointmentDate(String appointmentDate) {
            this.appointmentDate = appointmentDate;
            return this;
        }
        
        public Builder appointmentTime(String appointmentTime) {
            this.appointmentTime = appointmentTime;
            return this;
        }
        
        public Builder doctorName(String doctorName) {
            this.doctorName = doctorName;
            return this;
        }
        
        public Builder invoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }
        
        public Builder qrCodeBase64(String qrCodeBase64) {
            this.qrCodeBase64 = qrCodeBase64;
            return this;
        }
        
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }
        
        public Builder validFromTime(String validFromTime) {
            this.validFromTime = validFromTime;
            return this;
        }
        
        public Builder validUntilTime(String validUntilTime) {
            this.validUntilTime = validUntilTime;
            return this;
        }
        
        public AppointmentEmailRequest build() {
            return new AppointmentEmailRequest(this);
        }
    }
}
