package com.medframe.clinical.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Clinic domain entity representing a healthcare facility organizational unit.
 * 
 * <p>This is a pure domain model with business logic for validation and lifecycle management.
 * It follows hexagonal architecture principles with no infrastructure dependencies.
 * 
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Clinic code must be numeric only</li>
 *   <li>Clinic name must be alphanumeric only</li>
 *   <li>Clinic description must be alphanumeric only</li>
 *   <li>Clinic code must be unique across all clinics</li>
 *   <li>Clinic can be ACTIVE, INACTIVE, or DELETED</li>
 *   <li>Deletion is soft delete (sets estado to DELETED)</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clinic {
    
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\\s]+$");
    
    /**
     * Unique identifier for the clinic.
     */
    private String id;
    
    /**
     * Unique numeric code for the clinic (e.g., "101" for level 1 office 01).
     */
    private String codigo;
    
    /**
     * Name of the clinic.
     */
    private String nombre;
    
    /**
     * Description of the clinic.
     */
    private String descripcion;
    
    /**
     * Current operational status of the clinic.
     */
    private ClinicStatus estado;
    
    /**
     * Timestamp when the clinic record was created.
     */
    private LocalDateTime createdAt;
    
    /**
     * User identifier who created the clinic record.
     */
    private String createdBy;
    
    /**
     * Timestamp when the clinic record was last updated.
     */
    private LocalDateTime updatedAt;
    
    /**
     * User identifier who last updated the clinic record.
     */
    private String updatedBy;
    
    /**
     * Factory method to create a new clinic with validation.
     * 
     * <p>This method enforces all business rules for clinic creation:
     * <ul>
     *   <li>Validates codigo is numeric only</li>
     *   <li>Validates nombre is alphanumeric only</li>
     *   <li>Validates descripcion is alphanumeric only</li>
     *   <li>Sets initial estado to ACTIVE</li>
     *   <li>Sets audit fields (createdAt, createdBy)</li>
     * </ul>
     * 
     * @param codigo unique numeric code for the clinic
     * @param nombre name of the clinic
     * @param descripcion description of the clinic
     * @param createdBy user identifier who is creating the clinic
     * @return a new Clinic instance with validated data
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public static Clinic create(String codigo, String nombre, String descripcion, String createdBy) {
        validateCodigo(codigo);
        validateNombre(nombre);
        validateDescripcion(descripcion);
        
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo createdBy no puede estar vacío");
        }
        
        Clinic clinic = new Clinic();
        clinic.setId(java.util.UUID.randomUUID().toString());
        clinic.setCodigo(codigo);
        clinic.setNombre(nombre);
        clinic.setDescripcion(descripcion);
        clinic.setEstado(ClinicStatus.ACTIVE);
        clinic.setCreatedAt(LocalDateTime.now());
        clinic.setCreatedBy(createdBy);
        clinic.setUpdatedAt(LocalDateTime.now());
        clinic.setUpdatedBy(createdBy);
        
        return clinic;
    }
    
    /**
     * Updates the clinic with new data and validation.
     * 
     * <p>This method enforces all business rules for clinic updates:
     * <ul>
     *   <li>Validates codigo is numeric only (if changed)</li>
     *   <li>Validates nombre is alphanumeric only (if changed)</li>
     *   <li>Validates descripcion is alphanumeric only (if changed)</li>
     *   <li>Updates audit fields (updatedAt, updatedBy)</li>
     *   <li>Preserves original createdAt and createdBy values</li>
     * </ul>
     * 
     * @param codigo new clinic code (can be null to keep existing)
     * @param nombre new clinic name (can be null to keep existing)
     * @param descripcion new clinic description (can be null to keep existing)
     * @param estado new clinic status (can be null to keep existing)
     * @param updatedBy user identifier who is updating the clinic
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public void update(String codigo, String nombre, String descripcion, ClinicStatus estado, String updatedBy) {
        if (codigo != null) {
            validateCodigo(codigo);
            this.codigo = codigo;
        }
        
        if (nombre != null) {
            validateNombre(nombre);
            this.nombre = nombre;
        }
        
        if (descripcion != null) {
            validateDescripcion(descripcion);
            this.descripcion = descripcion;
        }
        
        if (estado != null) {
            this.estado = estado;
        }
        
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo updatedBy no puede estar vacío");
        }
        
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }
    
    /**
     * Performs a soft delete on the clinic.
     * 
     * <p>This method sets the estado to DELETED without physically removing the record.
     * This preserves historical data and maintains referential integrity.
     * All other field values are preserved.
     * 
     * @param updatedBy user identifier who is deleting the clinic
     * @throws IllegalArgumentException if updatedBy is null or empty
     */
    public void delete(String updatedBy) {
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo updatedBy no puede estar vacío");
        }
        
        this.estado = ClinicStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }
    
    /**
     * Validates that the clinic code is numeric only.
     * 
     * @param codigo the clinic code to validate
     * @throws IllegalArgumentException if codigo is null, empty, or contains non-numeric characters
     */
    private static void validateCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la clínica no puede estar vacío");
        }
        
        if (!NUMERIC_PATTERN.matcher(codigo).matches()) {
            throw new IllegalArgumentException(
                "El código de la clínica debe contener solo caracteres numéricos. Código recibido: " + codigo
            );
        }
    }
    
    /**
     * Validates that the clinic name is alphanumeric only.
     * 
     * @param nombre the clinic name to validate
     * @throws IllegalArgumentException if nombre is null, empty, or contains non-alphanumeric characters
     */
    private static void validateNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la clínica no puede estar vacío");
        }
        
        if (!ALPHANUMERIC_PATTERN.matcher(nombre).matches()) {
            throw new IllegalArgumentException(
                "El nombre de la clínica debe contener solo caracteres alfanuméricos. Nombre recibido: " + nombre
            );
        }
    }
    
    /**
     * Validates that the clinic description is alphanumeric only.
     * 
     * @param descripcion the clinic description to validate
     * @throws IllegalArgumentException if descripcion is null, empty, or contains non-alphanumeric characters
     */
    private static void validateDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción de la clínica no puede estar vacía");
        }
        
        if (!ALPHANUMERIC_PATTERN.matcher(descripcion).matches()) {
            throw new IllegalArgumentException(
                "La descripción de la clínica debe contener solo caracteres alfanuméricos. Descripción recibida: " + descripcion
            );
        }
    }
}
