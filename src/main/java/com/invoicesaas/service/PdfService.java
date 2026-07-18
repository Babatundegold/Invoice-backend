package com.invoicesaas.service;

import com.invoicesaas.config.FileStorageConfig;
import com.invoicesaas.entity.Company;
import com.invoicesaas.entity.Invoice;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;

/**
 * Renders an Invoice into a PDF file on disk. The generated file is what gets
 * attached to emails and what the WhatsApp/Telegram share links point to.
 */
@Service
@RequiredArgsConstructor
public class PdfService {

    private final TemplateEngine templateEngine;
    private final FileStorageConfig fileStorageConfig;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public String generateInvoicePdf(Invoice invoice) {
        Company company = invoice.getCompany();

        Context context = new Context();
        // logoUrl is only set on the template if the company actually uploaded one (optional)
        context.setVariable("logoUrl", company.getLogoUrl());
        context.setVariable("businessName", company.getBusinessName());
        context.setVariable("address", company.getAddress());
        context.setVariable("phone", company.getPhone());
        context.setVariable("invoiceNumber", invoice.getInvoiceNumber());
        context.setVariable("issueDate", invoice.getIssueDate());
        context.setVariable("dueDate", invoice.getDueDate());
        context.setVariable("clientName", invoice.getClient().getName());
        context.setVariable("clientEmail", invoice.getClient().getEmail());
        context.setVariable("clientAddress", invoice.getClient().getAddress());
        context.setVariable("items", invoice.getItems());
        context.setVariable("currency", company.getCurrency());
        context.setVariable("subtotal", invoice.getSubtotal());
        context.setVariable("taxAmount", invoice.getTaxAmount());
        context.setVariable("total", invoice.getTotal());
        context.setVariable("amountPaid", invoice.getAmountPaid());
        context.setVariable("notes", invoice.getNotes());
        context.setVariable("bankName", company.getBankName());
        context.setVariable("bankAccountNumber", company.getBankAccountNumber());
        context.setVariable("bankAccountName", company.getBankAccountName());
        context.setVariable("publicPayUrl", frontendBaseUrl + "/pay/" + invoice.getPublicToken());

        String html = templateEngine.process("invoice-template", context);

        String filename = "invoice-" + invoice.getInvoiceNumber() + "-" + invoice.getPublicToken().substring(0, 8) + ".pdf";
        Path outputPath = Path.of(fileStorageConfig.getPdfDir(), filename);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            HtmlConverter.convertToPdf(html, baos);
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                fos.write(baos.toByteArray());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }

        // Publicly downloadable URL - this is what gets shared via WhatsApp/Telegram/email
        return backendBaseUrl + "/files/pdfs/" + filename;
    }
}
