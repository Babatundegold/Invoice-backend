package com.invoicesaas.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceDto {

    @Data
    public static class ItemRequest {
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal taxRate;
        private BigDecimal discount;
    }

    @Data
    public static class CreateRequest {
        @NotNull
        private Long clientId;

        @NotEmpty
        private List<ItemRequest> items;

        private LocalDate issueDate;
        private LocalDate dueDate;
        private String notes;
        private boolean recurring;
        private String recurrenceInterval;
    }

    @Data
    public static class Response {
        private Long id;
        private String invoiceNumber;
        private String publicToken;
        private String publicUrl;
        private String clientName;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal total;
        private BigDecimal amountPaid;
        private String status;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private List<ItemRequest> items;
    }

    @Data
    public static class ShareLinksResponse {
        private String pdfDownloadUrl;
        private String whatsappShareUrl;
        private String telegramShareUrl;
        private String genericShareText;
    }
}
