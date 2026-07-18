package com.invoicesaas.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LedgerDto {
    private Long id;

    @NotNull
    private String type; // SALE, DEBT_RECEIVABLE, DEBT_PAYABLE, EXPENSE, PAYMENT

    private Long invoiceId;
    private String partyName;

    @NotNull
    private BigDecimal amount;

    private String status;
    private String category;
    private String paymentMethod;
    private LocalDate date;
    private String note;
}
