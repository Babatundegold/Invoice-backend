package org.example.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
public class PaymentWebhookController {

    @PostMapping("/paystack")
    public String handlePaystackWebhook(@RequestBody String payload) {

        System.out.println("Webhook received: " + payload);

        // TODO:
        // 1. Verify signature
        // 2. Parse JSON
        // 3. Update user to PREMIUM

        return "ok";
    }
}
