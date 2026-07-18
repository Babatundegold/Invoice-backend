package com.invoicesaas.entity;

public enum LedgerType {
    SALE,               // direct/cash sale, may or may not link to an invoice
    DEBT_RECEIVABLE,     // money owed TO the business by a customer
    DEBT_PAYABLE,        // money the business owes to a supplier/other party
    EXPENSE,
    PAYMENT              // repayment against a debt or invoice
}
