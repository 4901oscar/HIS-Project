package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.AppointmentQRData;

/**
 * Port for generating QR codes from appointment data.
 * Implementations should handle serialization to JSON and QR code generation.
 */
public interface QRCodeGenerator {
    
    /**
     * Generates a QR code containing appointment information.
     * 
     * @param appointmentData The appointment data to encode in the QR code
     * @return Base64-encoded PNG image of the QR code, or null if generation fails
     */
    String generateQRCode(AppointmentQRData appointmentData);
}
