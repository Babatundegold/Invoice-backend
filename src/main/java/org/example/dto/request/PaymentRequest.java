package org.example.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {
    private String email;
    private double amount;
}