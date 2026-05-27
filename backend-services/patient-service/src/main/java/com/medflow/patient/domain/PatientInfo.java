package com.medflow.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * PatientInfo value object representing patient personal information.
 * This is an embeddable component that will be embedded in the Appointment entity.
 * 
 * Validates patient data according to Guatemala standards:
 * - DPI: exactly 13 numeric digits
 * - NIT: "C/F" or 1-8 numeric digits
 * - Email: valid email format
 * - Phone: 8 numeric digits (Guatemala format)
 */
@Embeddable
public class PatientInfo {
    
    @Column(name = "dpi", nullable = false, length = 13)
    private String dpi;
    
    @Column(name = "nit", nullable = false, length = 20)
    private String nit;
    
    @Column(name = "primer_nombre", nullable = false, length = 100)
    private String primerNombre;
    
    @Column(name = "segundo_nombre", length = 100)
    private String segundoNombre;
    
    @Column(name = "primer_apellido", nullable = false, length = 100)
    private String primerApellido;
    
    @Column(name = "segundo_apellido", length = 100)
    private String segundoApellido;
    
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;
    
    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;
    
    @Column(name = "correo", nullable = false, length = 255)
    private String correo;
    
    // Default constructor for JPA
    protected PatientInfo() {
    }
    
    // Constructor with all fields
    public PatientInfo(String dpi, String nit, String primerNombre, String segundoNombre,
                       String primerApellido, String segundoApellido, LocalDate fechaNacimiento,
                       String telefono, String correo) {
        this.dpi = dpi;
        this.nit = nit;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.correo = correo;
    }
    
    /**
     * Validates all patient information fields.
     * Throws appropriate exceptions if validation fails.
     */
    public void validate() {
        validateDPI();
        validateNIT();
        validateEmail();
        validatePhone();
    }
    
    /**
     * Validates DPI format: exactly 13 numeric digits.
     * 
     * @throws InvalidDPIException if DPI is invalid
     */
    public void validateDPI() {
        if (dpi == null || !dpi.matches("\\d{13}")) {
            throw new InvalidDPIException("DPI must be exactly 13 numeric digits");
        }
    }
    
    /**
     * Validates NIT format: "C/F" or 1-8 numeric digits.
     * 
     * @throws InvalidNITException if NIT is invalid
     */
    public void validateNIT() {
        if (nit == null || (!nit.equals("C/F") && !nit.matches("\\d{1,8}"))) {
            throw new InvalidNITException("NIT must be 'C/F' or a valid numeric format (1-8 digits)");
        }
    }
    
    /**
     * Validates email format: must contain @ and valid domain structure.
     * 
     * @throws InvalidEmailException if email is invalid
     */
    public void validateEmail() {
        if (correo == null || !correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new InvalidEmailException("Email must be in a valid format");
        }
    }
    
    /**
     * Validates phone format: 8 numeric digits (Guatemala format).
     * 
     * @throws InvalidPhoneException if phone is invalid
     */
    public void validatePhone() {
        if (telefono == null || !telefono.matches("\\d{8}")) {
            throw new InvalidPhoneException("Phone must be exactly 8 numeric digits");
        }
    }
    
    /**
     * Returns the full name of the patient.
     * Concatenates first name, second name (if present), first last name, and second last name (if present).
     * 
     * @return full name as a single string
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        fullName.append(primerNombre);
        
        if (segundoNombre != null && !segundoNombre.isEmpty()) {
            fullName.append(" ").append(segundoNombre);
        }
        
        fullName.append(" ").append(primerApellido);
        
        if (segundoApellido != null && !segundoApellido.isEmpty()) {
            fullName.append(" ").append(segundoApellido);
        }
        
        return fullName.toString();
    }
    
    // Getters
    public String getDpi() {
        return dpi;
    }
    
    public String getNit() {
        return nit;
    }
    
    public String getPrimerNombre() {
        return primerNombre;
    }
    
    public String getSegundoNombre() {
        return segundoNombre;
    }
    
    public String getPrimerApellido() {
        return primerApellido;
    }
    
    public String getSegundoApellido() {
        return segundoApellido;
    }
    
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public String getCorreo() {
        return correo;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientInfo that = (PatientInfo) o;
        return Objects.equals(dpi, that.dpi) &&
               Objects.equals(nit, that.nit) &&
               Objects.equals(primerNombre, that.primerNombre) &&
               Objects.equals(segundoNombre, that.segundoNombre) &&
               Objects.equals(primerApellido, that.primerApellido) &&
               Objects.equals(segundoApellido, that.segundoApellido) &&
               Objects.equals(fechaNacimiento, that.fechaNacimiento) &&
               Objects.equals(telefono, that.telefono) &&
               Objects.equals(correo, that.correo);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dpi, nit, primerNombre, segundoNombre, primerApellido,
                          segundoApellido, fechaNacimiento, telefono, correo);
    }
    
    @Override
    public String toString() {
        return "PatientInfo{" +
               "dpi='" + dpi + '\'' +
               ", nit='" + nit + '\'' +
               ", fullName='" + getFullName() + '\'' +
               ", fechaNacimiento=" + fechaNacimiento +
               ", telefono='" + telefono + '\'' +
               ", correo='" + correo + '\'' +
               '}';
    }
}
