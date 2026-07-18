package com.invoicesaas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    @Value("${app.file-storage.pdf-dir}")
    private String pdfDir;

    // Comma-separated list of allowed frontend origins, e.g. https://invoicepro.vercel.app
    // Defaults to "*" for local dev - set APP_CORS_ALLOWED_ORIGINS in Railway once the
    // frontend is deployed so only your real domain can call this API.
    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/logos/**")
                .addResourceLocations("file:" + uploadDir + "/");

        registry.addResourceHandler("/files/pdfs/**")
                .addResourceLocations("file:" + pdfDir + "/");
    }
}
