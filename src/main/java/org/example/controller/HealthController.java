package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    // This handles the Cron-job (Very light, no database work)
    @GetMapping("/ping")
    public String ping() {
        return "Alive";
    }

    // This handles anyone visiting the main link (A professional "Welcome")
    @GetMapping("/")
    public String home() {
        return "InvoicePro Backend is running successfully!";
    }
}