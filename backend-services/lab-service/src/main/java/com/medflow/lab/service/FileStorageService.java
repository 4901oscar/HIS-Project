package com.medflow.lab.service;

import com.medflow.lab.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for handling file storage operations.
 * Provides methods to store files with unique filenames to prevent collisions.
 * 
 * Requirements validated: 11.3, 11.4, 11.7
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${lab.results.storage-path}")
    private String storagePath;

    /**
     * Stores a file to the configured storage path with a unique filename.
     * 
     * @param file The multipart file to store
     * @param uniqueFilename The unique filename to use for storage
     * @return The absolute path where the file was stored
     * @throws FileStorageException if file storage fails
     */
    public String store(MultipartFile file, String uniqueFilename) {
        try {
            // Create storage directory if it doesn't exist
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                log.info("Creando directorio de almacenamiento: {}", storageDir);
                Files.createDirectories(storageDir);
            }

            // Resolve the full file path
            Path filePath = storageDir.resolve(uniqueFilename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Archivo guardado exitosamente en: {}", filePath);
            return filePath.toString();

        } catch (IOException e) {
            log.error("Error al guardar archivo: {}", e.getMessage(), e);
            throw new FileStorageException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a unique filename using UUID + timestamp + original file extension.
     * Format: UUID_timestamp.extension
     * 
     * Example: 550e8400-e29b-41d4-a716-446655440000_20240115103045.pdf
     * 
     * @param originalFilename The original filename from the uploaded file
     * @return A unique filename that prevents collisions
     */
    public String generateUniqueFilename(String originalFilename) {
        // Extract file extension
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate UUID
        String uuid = UUID.randomUUID().toString();

        // Generate timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // Combine: UUID_timestamp.extension
        String uniqueFilename = uuid + "_" + timestamp + extension;

        log.debug("Nombre de archivo único generado: {} desde: {}", uniqueFilename, originalFilename);
        return uniqueFilename;
    }

    /**
     * Gets the configured storage path.
     * 
     * @return The storage path
     */
    public String getStoragePath() {
        return storagePath;
    }
}
