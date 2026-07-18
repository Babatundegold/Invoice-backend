package com.invoicesaas.service;

import com.invoicesaas.dto.LedgerDto;
import com.invoicesaas.entity.*;
import com.invoicesaas.exception.ResourceNotFoundException;
import com.invoicesaas.repository.InvoiceRepository;
import com.invoicesaas.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * The "book" - a general record of sales, debts (owed to the business and owed
 * by the business), and expenses, independent of (but optionally linked to)
 * formal invoices. Gives the user one place to see their whole financial picture.
 */
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final InvoiceRepository invoiceRepository;
    private final CompanyService companyService;

    public LedgerEntry create(Long ownerId, LedgerDto dto) {
        Company company = companyService.getByOwnerId(ownerId);

        Invoice invoice = null;
        if (dto.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(dto.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        }

        LedgerEntry entry = LedgerEntry.builder()
                .company(company)
                .type(LedgerType.valueOf(dto.getType()))
                .invoice(invoice)
                .partyName(dto.getPartyName())
                .amount(dto.getAmount())
                .category(dto.getCategory())
                .paymentMethod(dto.getPaymentMethod())
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .note(dto.getNote())
                .status(dto.getStatus() != null ? LedgerStatus.valueOf(dto.getStatus()) : LedgerStatus.OPEN)
                .build();

        entry.setBalanceAfter(computeRunningBalance(company.getId(), entry.getAmount(), entry.getType()));

        return ledgerEntryRepository.save(entry);
    }

    // Auto-log a receivable entry whenever an invoice is created, so the book
    // reflects it without the user having to double-enter it.
    public void recordInvoiceCreated(Invoice invoice) {
        LedgerEntry entry = LedgerEntry.builder()
                .company(invoice.getCompany())
                .type(LedgerType.DEBT_RECEIVABLE)
                .invoice(invoice)
                .partyName(invoice.getClient().getName())
                .amount(invoice.getTotal())
                .status(LedgerStatus.OPEN)
                .date(invoice.getIssueDate())
                .note("Auto-logged from invoice " + invoice.getInvoiceNumber())
                .build();
        ledgerEntryRepository.save(entry);
    }

    // Auto-log a payment entry when an invoice gets paid (via Paystack or manually)
    public void recordPaymentReceived(Invoice invoice, BigDecimal amount, String method) {
        LedgerEntry entry = LedgerEntry.builder()
                .company(invoice.getCompany())
                .type(LedgerType.PAYMENT)
                .invoice(invoice)
                .partyName(invoice.getClient().getName())
                .amount(amount)
                .paymentMethod(method)
                .status(LedgerStatus.CLEARED)
                .date(LocalDate.now())
                .note("Payment for invoice " + invoice.getInvoiceNumber())
                .build();
        ledgerEntryRepository.save(entry);
    }

    public List<LedgerEntry> list(Long ownerId, String type) {
        Company company = companyService.getByOwnerId(ownerId);
        if (type != null && !type.isBlank()) {
            return ledgerEntryRepository.findByCompanyIdAndTypeOrderByDateDesc(company.getId(), LedgerType.valueOf(type));
        }
        return ledgerEntryRepository.findByCompanyIdOrderByDateDescCreatedAtDesc(company.getId());
    }

    public List<LedgerEntry> listByDateRange(Long ownerId, LocalDate start, LocalDate end) {
        Company company = companyService.getByOwnerId(ownerId);
        return ledgerEntryRepository.findByCompanyIdAndDateBetween(company.getId(), start, end);
    }

    public LedgerEntry updateStatus(Long ownerId, Long entryId, String status) {
        Company company = companyService.getByOwnerId(ownerId);
        LedgerEntry entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger entry not found"));
        if (!entry.getCompany().getId().equals(company.getId())) {
            throw new ResourceNotFoundException("Ledger entry not found");
        }
        entry.setStatus(LedgerStatus.valueOf(status));
        return ledgerEntryRepository.save(entry);
    }

    public void delete(Long ownerId, Long entryId) {
        Company company = companyService.getByOwnerId(ownerId);
        LedgerEntry entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger entry not found"));
        if (!entry.getCompany().getId().equals(company.getId())) {
            throw new ResourceNotFoundException("Ledger entry not found");
        }
        ledgerEntryRepository.delete(entry);
    }

    // Simple running-balance calc: sales/receivables add to balance, expenses/payables/payments reduce it
    private BigDecimal computeRunningBalance(Long companyId, BigDecimal amount, LedgerType type) {
        List<LedgerEntry> all = ledgerEntryRepository.findByCompanyIdOrderByDateDescCreatedAtDesc(companyId);
        BigDecimal current = all.isEmpty() ? BigDecimal.ZERO : (all.get(0).getBalanceAfter() != null ? all.get(0).getBalanceAfter() : BigDecimal.ZERO);

        return switch (type) {
            case SALE, DEBT_RECEIVABLE -> current.add(amount);
            case EXPENSE, DEBT_PAYABLE, PAYMENT -> current.subtract(amount);
        };
    }
}
