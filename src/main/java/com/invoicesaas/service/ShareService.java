package com.invoicesaas.service;

import com.invoicesaas.dto.InvoiceDto;
import com.invoicesaas.entity.Invoice;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Builds shareable links for the generated invoice PDF.
 *
 * IMPORTANT (practical note): WhatsApp and Telegram don't offer a way for a backend
 * to silently "push" a file into someone's chat unless:
 *   - WhatsApp: you're approved for the Meta WhatsApp Business Cloud API, OR
 *   - Telegram: you use a Telegram Bot API with the recipient's chat_id (bot must be started by them first)
 *
 * Without those integrations, the practical + zero-approval-needed approach (used here) is:
 *   1. Generate the PDF and host it at a public URL (done in PdfService)
 *   2. Build a "click to share" deep link (wa.me / t.me) pre-filled with a message + the PDF link
 *   3. The frontend opens that link - WhatsApp/Telegram/any app opens with the message ready to send
 *
 * This works for WhatsApp, Telegram, Email, SMS, Twitter/X, and "copy link" with zero extra
 * approvals or cost. If you later get WhatsApp Business API / a Telegram bot set up, swap in
 * TelegramBotService (stubbed below) to send the file directly instead of just deep-linking.
 */
@Service
@RequiredArgsConstructor
public class ShareService {

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public InvoiceDto.ShareLinksResponse buildShareLinks(Invoice invoice) {
        String pdfUrl = invoice.getPdfPath();
        String publicViewUrl = frontendBaseUrl + "/invoice/" + invoice.getPublicToken();

        String message = "Invoice " + invoice.getInvoiceNumber() + " from "
                + invoice.getCompany().getBusinessName() + " - " + publicViewUrl
                + (pdfUrl != null ? ("\nDownload PDF: " + pdfUrl) : "");

        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

        InvoiceDto.ShareLinksResponse response = new InvoiceDto.ShareLinksResponse();
        response.setPdfDownloadUrl(pdfUrl);
        response.setWhatsappShareUrl("https://wa.me/?text=" + encodedMessage);
        response.setTelegramShareUrl("https://t.me/share/url?url=" + URLEncoder.encode(publicViewUrl, StandardCharsets.UTF_8)
                + "&text=" + encodedMessage);
        response.setGenericShareText(message);
        return response;
    }
}
