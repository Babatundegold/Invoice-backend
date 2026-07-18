package com.invoicesaas.dto;

import lombok.Data;

@Data
public class CompanyDto {
    private String businessName;
    private String address;
    private String phone;
    private String taxId;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String currency;
    private String invoicePrefix;
    private String logoUrl; // read-only, set by upload endpoint
}
