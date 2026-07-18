package com.invoicesaas.service;

import com.invoicesaas.config.FileStorageConfig;
import com.invoicesaas.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    private static final List<String> ALLOWED_LOGO_TYPES = List.of("image/png", "image/jpeg", "image/jpg", "image/svg+xml");
    private static final long MAX_LOGO_SIZE = 2 * 1024 * 1024; // 2MB

    /**
     * Logo upload is OPTIONAL at the company-profile level (see CompanyService) -
     * this method only handles the physical file storage validation + save.
     */
    public String storeLogo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file provided");
        }
        if (!ALLOWED_LOGO_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Logo must be PNG, JPG or SVG");
        }
        if (file.getSize() > MAX_LOGO_SIZE) {
            throw new BadRequestException("Logo must be smaller than 2MB");
        }

        try {
            String extension = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;
            Path target = Path.of(fileStorageConfig.getUploadDir(), filename);
            Files.copy(file.getInputStream(), target);
            return backendBaseUrl + "/files/logos/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store logo: " + e.getMessage());
        }
    }

    public void deleteLogo(String logoUrl) {
        if (logoUrl == null) return;
        try {
            String filename = logoUrl.substring(logoUrl.lastIndexOf("/") + 1);
            Files.deleteIfExists(Path.of(fileStorageConfig.getUploadDir(), filename));
        } catch (IOException ignored) {
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
