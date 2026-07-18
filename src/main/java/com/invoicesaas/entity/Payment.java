package com.invoicesaas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    private String paystackReference;

    private BigDecimal amount;

    @Builder.Default
    private String status = "PENDING"; // PENDING, SUCCESS, FAILED

    @Builder.Default
    private String channel = "paystack"; // paystack or manual

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
