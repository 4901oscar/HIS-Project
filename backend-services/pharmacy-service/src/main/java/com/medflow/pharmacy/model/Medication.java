package com.medflow.pharmacy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un medicamento en el catálogo de la farmacia.
 * 
 * Incluye información de inventario (stock actual y mínimo) para alertas.
 */
@Entity
@Table(name = "medications", schema = "pharmacy_schema", indexes = {
    @Index(name = "idx_medications_stock", columnList = "current_stock")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false, length = 50)
    private String unit; // tablets, ml, capsules, etc.
    
    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;
    
    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 10;
    
    @Column(nullable = false)
    private boolean active = true;
}
