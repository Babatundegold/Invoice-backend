package com.invoicesaas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The "book" — a running record of sales, debts owed/payable, expenses and payments.
 * Acts as a unified ledger so business owners have a single record of their finances,
 * similar to a traditional record book.
 */
@Entity
@Table(name = "ledger_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerType type;

    // Optional link back to a formal invoice, if this entry originated from one
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    private String partyName; // customer or supplier name, optional for cash sales

    @Column(nullable = false)
    private BigDecimal amount;

    private BigDecimal balanceAfter; // running balance for this entry, optional but nice for a "book" feel

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LedgerStatus status = LedgerStatus.OPEN;

    private String category; // for expenses: rent, supplies, transport, etc.
    private String paymentMethod; // cash, transfer, paystack, etc.

    @Column(nullable = false)
    private LocalDate date;

    private String note;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
