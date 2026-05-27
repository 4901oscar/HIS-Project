package com.medflow.lab.service;

import com.medflow.lab.exception.FileStorageException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for FileStorageService.
 * Tests file storage operations, unique filename generation, and exception handling.
 */
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "storagePath", tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up any files created during tests
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
        }
    }

    @Test
    void shouldStoreFileSuccessfully() throws IOException {
        // Given
        String content = "Test PDF content";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                content.getBytes()
        );
        String uniqueFilename = "test-unique-file.pdf";

        // When
        String storedPath = fileStorageService.store(file, uniqueFilename);

        // Then
        assertThat(storedPath).isNotNull();
        Path storedFile = Paths.get(storedPath);
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readString(storedFile)).isEqualTo(content);
    }

    @Test
    void shouldCreateStorageDirectoryIfNotExists() throws IOException {
        // Given
        Path newStorageDir = tempDir.resolve("new-storage");
        ReflectionTestUtils.setField(fileStorageService, "storagePath", newStorageDir.toString());
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "content".getBytes()
        );
        String uniqueFilename = "test.pdf";

        // When
        String storedPath = fileStorageService.store(file, uniqueFilename);

        // Then
        assertThat(Files.exists(newStorageDir)).isTrue();
        assertThat(Files.exists(Paths.get(storedPath))).isTrue();
    }

    @Test
    void shouldReplaceExistingFileWithSameName() throws IOException {
        // Given
        String uniqueFilename = "duplicate.pdf";
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "original.pdf",
                "application/pdf",
                "First content".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "original.pdf",
                "application/pdf",
                "Second content".getBytes()
        );

        // When
        String path1 = fileStorageService.store(file1, uniqueFilename);
        String path2 = fileStorageService.store(file2, uniqueFilename);

        // Then
        assertThat(path1).isEqualTo(path2);
        assertThat(Files.readString(Paths.get(path2))).isEqualTo("Second content");
    }

    @Test
    void shouldThrowFileStorageExceptionWhenIOErrorOccurs() {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "content".getBytes()
        ) {
            @Override
            public java.io.InputStream getInputStream() throws IOException {
                throw new IOException("Simulated IO error");
            }
        };
        String uniqueFilename = "test.pdf";

        // When/Then
        assertThatThrownBy(() -> fileStorageService.store(file, uniqueFilename))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Error al guardar el archivo");
    }

    @Test
    void shouldGenerateUniqueFilenameWithPdfExtension() {
        // Given
        String originalFilename = "document.pdf";

        // When
        String uniqueFilename = fileStorageService.generateUniqueFilename(originalFilename);

        // Then
        assertThat(uniqueFilename).isNotNull();
        assertThat(uniqueFilename).endsWith(".pdf");
        assertThat(uniqueFilename).contains("_"); // UUID_timestamp separator
        assertThat(uniqueFilename).matches("^[a-f0-9\\-]+_\\d{14}\\.pdf$");
    }

    @Test
    void shouldGenerateUniqueFilenameWithJpegExtension() {
        // Given
        String originalFilename = "image.jpeg";

        // When
        String uniqueFilename = fileStorageService.generateUniqueFilename(originalFilename);

        // Then
        assertThat(uniqueFilename).endsWith(".jpeg");
        assertThat(uniqueFilename).matches("^[a-f0-9\\-]+_\\d{14}\\.jpeg$");
    }

    @Test
    void shouldGenerateUniqueFilenameWithPngExtension() {
        // Given
        String originalFilename = "screenshot.png";

        // When
        String uniqueFilename = fileStorageService.generateUniqueFilename(originalFilename);

        // Then
        assertThat(uniqueFilename).endsWith(".png");
        assertThat(uniqueFilename).matches("^[a-f0-9\\-]+_\\d{14}\\.png$");
    }

    @Test
    void shouldGenerateUniqueFilenameWithoutExtensionWhenOriginalHasNone() {
        // Given
        String originalFilename = "document";

        // When
        String uniqueFilename = fileStorageService.generateUniqueFilename(originalFilename);

        // Then
        assertThat(uniqueFilename).isNotNull();
        assertThat(uniqueFilename).doesNotContain(".");
        assertThat(uniqueFilename).matches("^[a-f0-9\\-]+_\\d{14}$");
    }

    @Test
    void shouldGenerateUniqueFilenameWhenOriginalIsNull() {
        // Given
        String originalFilename = null;

        // When
        String uniqueFilename = fileStorageService.generateUniqueFilename(originalFilename);

        // Then
        assertThat(uniqueFilename).isNotNull();
        assertThat(uniqueFilename).doesNotContain(".");
        assertThat(uniqueFilename).matches("^[a-f0-9\\-]+_\\d{14}$");
    }

    @Test
    void shouldGenerateDifferentFilenamesForMultipleCalls() {
        // Given
        String originalFilename = "test.pdf";
        Set<String> generatedFilenames = new HashSet<>();

        // When
        for (int i = 0; i < 100; i++) {
            String uniqueFilename = fileStorageService.generateUniqueFilename(originalFilename);
            generatedFilenames.add(uniqueFilename);
        }

        // Then - All filenames should be unique
        assertThat(generatedFilenames).hasSize(100);
    }

    @Test
    void shouldReturnConfiguredStoragePath() {
        // When
        String storagePath = fileStorageService.getStoragePath();

        // Then
        assertThat(storagePath).isEqualTo(tempDir.toString());
    }

    @Test
    void shouldHandleFilenameWithMultipleDots() {
        // Given
        String originalFilename = "my.document.with.dots.pdf";

        // When
        String uniqueFilename = fileStorageService.generateUniqueFilename(originalFilename);

        // Then
        assertThat(uniqueFilename).endsWith(".pdf");
        assertThat(uniqueFilename).matches("^[a-f0-9\\-]+_\\d{14}\\.pdf$");
    }

    @Test
    void shouldStoreMultipleFilesWithDifferentNames() throws IOException {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "test1.pdf",
                "application/pdf",
                "Content 1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "test2.pdf",
                "application/pdf",
                "Content 2".getBytes()
        );
        String filename1 = fileStorageService.generateUniqueFilename("test1.pdf");
        String filename2 = fileStorageService.generateUniqueFilename("test2.pdf");

        // When
        String path1 = fileStorageService.store(file1, filename1);
        String path2 = fileStorageService.store(file2, filename2);

        // Then
        assertThat(path1).isNotEqualTo(path2);
        assertThat(Files.exists(Paths.get(path1))).isTrue();
        assertThat(Files.exists(Paths.get(path2))).isTrue();
        assertThat(Files.readString(Paths.get(path1))).isEqualTo("Content 1");
        assertThat(Files.readString(Paths.get(path2))).isEqualTo("Content 2");
    }
}
