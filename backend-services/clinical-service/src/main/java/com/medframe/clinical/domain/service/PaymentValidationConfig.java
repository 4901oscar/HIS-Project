package com.medframe.clinical.domain.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración para la validación de pago antes de activar citas.
 * 
 * <p>Esta configuración permite habilitar o deshabilitar la validación de pago
 * mediante un feature flag, facilitando despliegues graduales y rollback rápido.</p>
 * 
 * <p><strong>Propiedades configurables:</strong></p>
 * <ul>
 *   <li><strong>payment.validation.enabled:</strong> Habilita/deshabilita validación (default: true)</li>
 * </ul>
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-9.1: Leer configuración desde application.yml</li>
 *   <li>REQ-9.2: Ejecutar validación cuando enabled = true</li>
 *   <li>REQ-9.3: Omitir validación cuando enabled = false</li>
 *   <li>REQ-9.4: Valor por defecto true</li>
 * </ul>
 * 
 * @author MedFlow Development Team
 * @version 1.0
 * @since 2025-01-20
 */
@Component
@ConfigurationProperties(prefix = "payment.validation")
@Data
public class PaymentValidationConfig {
    
    /**
     * Feature flag para habilitar/deshabilitar la validación de pago.
     * 
     * <p>Cuando está en false, las citas pueden activarse sin validar el estado de pago,
     * permitiendo rollback rápido en caso de problemas con la integración.</p>
     * 
     * <p>Default: true (validación habilitada)</p>
     */
    private boolean enabled = true;
}
