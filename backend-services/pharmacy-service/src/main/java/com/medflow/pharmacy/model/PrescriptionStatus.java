package com.medflow.pharmacy.model;

/**
 * Estado de una prescripción médica en el sistema de farmacia.
 * 
 * Estados válidos:
 * - PENDING: Receta recibida, pendiente de despacho
 * - DISPENSED: Medicamentos despachados al paciente
 * - CANCELLED: Receta cancelada
 */
public enum PrescriptionStatus {
    PENDING,
    DISPENSED,
    CANCELLED
}
