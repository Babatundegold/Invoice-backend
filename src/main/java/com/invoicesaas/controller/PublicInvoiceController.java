package com.invoicesaas.controller;

import com.invoicesaas.entity.Invoice;
import com.invoicesaas.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * No-login endpoints, reached via the public invoice link that gets shared
 * over WhatsApp/Telegram/email. A client clicking the link can view the
 * invoice and pay it without ever creating an account.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicInvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/invoices/{token}")
    public ResponseEntity<Invoice> viewInvoice(@PathVariable String token) {
        return ResponseEntity.ok(invoiceService.getByPublicToken(token));
    }
}
