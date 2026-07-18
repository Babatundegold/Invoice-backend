package com.invoicesaas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO; // percentage
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO; // flat amount

    private BigDecimal lineTotal;
}
