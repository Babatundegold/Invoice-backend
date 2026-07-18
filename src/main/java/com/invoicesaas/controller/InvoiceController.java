package com.invoicesaas.controller;

import com.invoicesaas.dto.InvoiceDto;
import com.invoicesaas.entity.Invoice;
import com.invoicesaas.entity.InvoiceStatus;
import com.invoicesaas.security.CurrentUser;
import com.invoicesaas.service.InvoiceService;
import com.invoicesaas.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ShareService shareService;

    @PostMapping
    public ResponseEntity<Invoice> create(@Valid @RequestBody InvoiceDto.CreateRequest request) {
        return ResponseEntity.ok(invoiceService.create(CurrentUser.id(), request));
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> list(@RequestParam(required = false) InvoiceStatus status) {
        return ResponseEntity.ok(invoiceService.list(CurrentUser.id(), status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> get(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.get(CurrentUser.id(), id));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<Invoice> markAsSent(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.markAsSent(CurrentUser.id(), id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Invoice> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.cancel(CurrentUser.id(), id));
    }

    // Generates/regenerates the PDF for this invoice and stores its public URL
    @PostMapping("/{id}/pdf")
    public ResponseEntity<Invoice> generatePdf(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.generatePdf(CurrentUser.id(), id));
    }

    /**
     * Returns ready-to-use share links: direct PDF download URL, a WhatsApp
     * "click to share" link (wa.me), and a Telegram share link (t.me).
     * The frontend just needs to open these URLs (e.g. window.open) - no extra
     * backend work needed to "send" via WhatsApp/Telegram this way.
     */
    @GetMapping("/{id}/share")
    public ResponseEntity<InvoiceDto.ShareLinksResponse> getShareLinks(@PathVariable Long id) {
        Invoice invoice = invoiceService.get(CurrentUser.id(), id);
        if (invoice.getPdfPath() == null) {
            invoice = invoiceService.generatePdf(CurrentUser.id(), id);
        }
        return ResponseEntity.ok(shareService.buildShareLinks(invoice));
    }
}
