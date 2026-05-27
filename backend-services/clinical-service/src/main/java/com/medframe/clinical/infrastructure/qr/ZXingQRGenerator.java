package com.medframe.clinical.infrastructure.qr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.medframe.clinical.domain.model.AppointmentQRData;
import com.medframe.clinical.domain.port.out.QRCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * ZXing-based implementation of QR code generator.
 * Generates QR codes containing appointment data in JSON format,
 * encoded as base64 PNG images.
 */
@Component
public class ZXingQRGenerator implements QRCodeGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(ZXingQRGenerator.class);
    private static final int QR_SIZE = 300;
    
    private final ObjectMapper objectMapper;
    
    public ZXingQRGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String generateQRCode(AppointmentQRData appointmentData) {
        try {
            // 1. Serialize appointment data to JSON
            String jsonContent = objectMapper.writeValueAsString(appointmentData);
            
            // 2. Configure QR code hints
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L); // 15% recovery
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            
            // 3. Generate QR code matrix
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                jsonContent, 
                BarcodeFormat.QR_CODE, 
                QR_SIZE, 
                QR_SIZE, 
                hints
            );
            
            // 4. Convert matrix to PNG image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            // 5. Encode image as base64
            byte[] imageBytes = outputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            log.info("QR code generated successfully for appointment {}", 
                     appointmentData.getAppointmentId());
            return base64Image;
            
        } catch (WriterException e) {
            log.error("Failed to generate QR code for appointment {}: QR encoding error - {}", 
                      appointmentData.getAppointmentId(), e.getMessage(), e);
            return null; // Graceful degradation
        } catch (IOException e) {
            log.error("Failed to generate QR code for appointment {}: I/O error - {}", 
                      appointmentData.getAppointmentId(), e.getMessage(), e);
            return null; // Graceful degradation
        } catch (Exception e) {
            log.error("Failed to generate QR code for appointment {}: Unexpected error - {}", 
                      appointmentData.getAppointmentId(), e.getMessage(), e);
            return null; // Graceful degradation
        }
    }
}
