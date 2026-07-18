package com.invoicesaas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private User owner;

    @Column(nullable = false)
    private String businessName;

    // Logo is OPTIONAL - nullable, invoices fall back to business name text if null
    private String logoUrl;

    private String address;
    private String phone;
    private String taxId;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;

    @Builder.Default
    private String currency = "NGN";

    private String invoicePrefix; // e.g. "INV-"
}
