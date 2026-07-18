package com.invoicesaas.dto;

import lombok.Data;

import java.math.BigDecimal;

public class PaymentDto {

    @Data
    public static class InitRequest {
        private Long invoiceId;
    }

    @Data
    public static class InitResponse {
        private String authorizationUrl;
        private String accessCode;
        private String reference;
    }

    @Data
    public static class ManualPaymentRequest {
        private Long invoiceId;
        private BigDecimal amount;
        private String note;
    }
}
