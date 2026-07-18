package com.invoicesaas.service;

import com.invoicesaas.dto.InvoiceDto;
import com.invoicesaas.entity.*;
import com.invoicesaas.exception.BadRequestException;
import com.invoicesaas.exception.ResourceNotFoundException;
import com.invoicesaas.repository.ClientRepository;
import com.invoicesaas.repository.InvoiceRepository;
import com.invoicesaas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final long FREE_TIER_MONTHLY_LIMIT = 5;

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final PdfService pdfService;
    private final LedgerService ledgerService;

    public Invoice create(Long ownerId, InvoiceDto.CreateRequest request) {
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Company company = companyService.getByOwnerId(ownerId);

        enforceFreeTierLimit(user, company);

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        if (!client.getCompany().getId().equals(company.getId())) {
            throw new ResourceNotFoundException("Client not found");
        }

        Invoice invoice = Invoice.builder()
                .company(company)
                .client(client)
                .invoiceNumber(generateInvoiceNumber(company))
                .issueDate(request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now())
                .dueDate(request.getDueDate())
                .notes(request.getNotes())
                .recurring(request.isRecurring())
                .recurrenceInterval(request.getRecurrenceInterval())
                .status(InvoiceStatus.DRAFT)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;

        for (InvoiceDto.ItemRequest itemReq : request.getItems()) {
            BigDecimal qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : BigDecimal.ONE;
            BigDecimal price = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal taxRate = itemReq.getTaxRate() != null ? itemReq.getTaxRate() : BigDecimal.ZERO;
            BigDecimal discount = itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO;

            BigDecimal lineSubtotal = qty.multiply(price).subtract(discount);
            BigDecimal lineTax = lineSubtotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = lineSubtotal.add(lineTax);

            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .description(itemReq.getDescription())
                    .quantity(qty)
                    .unitPrice(price)
                    .taxRate(taxRate)
                    .discount(discount)
                    .lineTotal(lineTotal)
                    .build();

            invoice.getItems().add(item);
            subtotal = subtotal.add(lineSubtotal);
            taxTotal = taxTotal.add(lineTax);
        }

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxTotal);
        invoice.setTotal(subtotal.add(taxTotal));

        Invoice saved = invoiceRepository.save(invoice);

        // Record it in the "book" (ledger) as a receivable so it shows up in the running record
        ledgerService.recordInvoiceCreated(saved);

        return saved;
    }

    public Invoice get(Long ownerId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        validateOwnership(ownerId, invoice);
        return invoice;
    }

    public Invoice getByPublicToken(String token) {
        return invoiceRepository.findByPublicToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    public List<Invoice> list(Long ownerId, InvoiceStatus status) {
        Company company = companyService.getByOwnerId(ownerId);
        if (status != null) {
            return invoiceRepository.findByCompanyIdAndStatus(company.getId(), status);
        }
        return invoiceRepository.findByCompanyId(company.getId());
    }

    public Invoice markAsSent(Long ownerId, Long invoiceId) {
        Invoice invoice = get(ownerId, invoiceId);
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setUpdatedAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    public Invoice cancel(Long ownerId, Long invoiceId) {
        Invoice invoice = get(ownerId, invoiceId);
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return invoiceRepository.save(invoice);
    }

    /**
     * Generates (or regenerates) the PDF for an invoice and stores its path.
     * This PDF URL is what powers WhatsApp/Telegram/email/social sharing.
     */
    public Invoice generatePdf(Long ownerId, Long invoiceId) {
        Invoice invoice = get(ownerId, invoiceId);
        String pdfUrl = pdfService.generateInvoicePdf(invoice);
        invoice.setPdfPath(pdfUrl);
        return invoiceRepository.save(invoice);
    }

    private void enforceFreeTierLimit(User user, Company company) {
        if (user.isPremium()) return;

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        long count = invoiceRepository.countByCompanyIdAndCreatedAtBetween(company.getId(), start, end);
        if (count >= FREE_TIER_MONTHLY_LIMIT) {
            throw new BadRequestException(
                "Free plan allows " + FREE_TIER_MONTHLY_LIMIT + " invoices per month. Upgrade to Premium for unlimited invoices."
            );
        }
    }

    private String generateInvoiceNumber(Company company) {
        long count = invoiceRepository.countByCompanyId(company.getId()) + 1;
        String prefix = company.getInvoicePrefix() != null ? company.getInvoicePrefix() : "INV-";
        return prefix + String.format("%04d", count);
    }

    private void validateOwnership(Long ownerId, Invoice invoice) {
        Company company = companyService.getByOwnerId(ownerId);
        if (!invoice.getCompany().getId().equals(company.getId())) {
            throw new ResourceNotFoundException("Invoice not found");
        }
    }
}
