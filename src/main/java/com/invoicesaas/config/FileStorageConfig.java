package com.invoicesaas.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Getter
@Component
public class FileStorageConfig {

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    @Value("${app.file-storage.pdf-dir}")
    private String pdfDir;

    @PostConstruct
    public void init() {
        new File(uploadDir).mkdirs();
        new File(pdfDir).mkdirs();
    }
}
