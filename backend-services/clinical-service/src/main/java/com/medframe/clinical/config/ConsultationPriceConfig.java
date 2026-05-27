package com.medframe.clinical.config;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Configuración de precios de consulta médica.
 * 
 * <p>Esta clase centraliza la configuración de precios para diferentes tipos de consulta,
 * permitiendo ajustes sin recompilar el código. Los precios se expresan en GTQ (Quetzales).</p>
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-4.1: Precio de consulta configurable externamente</li>
 *   <li>REQ-4.2: Precio por defecto de 150.00 GTQ</li>
 *   <li>REQ-4.3: Validación de precio mayor a cero</li>
 *   <li>REQ-4.4: Precio aplicado automáticamente al crear factura</li>
 * </ul>
 * 
 * <p><strong>Configuración en application.yml:</strong></p>
 * <pre>
 * consultation:
 *   price:
 *     default: 150.00
 *     emergency: 300.00
 *     followup: 100.00
 * </pre>
 * 
 * @author MedFlow Development Team
 * @version 1.0
 * @since 2024-04-24
 */
@Configuration
@ConfigurationProperties(prefix = "consultation.price")
@Validated
@Data
public class ConsultationPriceConfig {

    /**
     * Precio por defecto para consultas médicas generales.
     * Debe ser mayor a cero. Valor por defecto: 150.00 GTQ.
     */
    @DecimalMin(value = "0.01", message = "El precio de consulta debe ser mayor a cero")
    private BigDecimal defaultPrice = new BigDecimal("150.00");

    /**
     * Precio para consultas de emergencia.
     * Debe ser mayor a cero. Valor por defecto: 300.00 GTQ.
     */
    @DecimalMin(value = "0.01", message = "El precio de consulta de emergencia debe ser mayor a cero")
    private BigDecimal emergency = new BigDecimal("300.00");

    /**
     * Precio para consultas de seguimiento.
     * Debe ser mayor a cero. Valor por defecto: 100.00 GTQ.
     */
    @DecimalMin(value = "0.01", message = "El precio de consulta de seguimiento debe ser mayor a cero")
    private BigDecimal followup = new BigDecimal("100.00");

    /**
     * Precio para órdenes de laboratorio.
     * Debe ser mayor a cero. Valor por defecto: 75.00 GTQ.
     */
    @DecimalMin(value = "0.01", message = "El precio de laboratorio debe ser mayor a cero")
    private BigDecimal lab = new BigDecimal("75.00");

    /**
     * Obtiene el precio de consulta por defecto.
     * 
     * <p>Este método es utilizado por el servicio de citas para determinar
     * el precio a aplicar al crear una factura automáticamente.</p>
     * 
     * @return Precio de consulta por defecto en GTQ
     */
    public BigDecimal getPrice() {
        return defaultPrice;
    }
}
