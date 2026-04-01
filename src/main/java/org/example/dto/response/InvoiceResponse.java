package org.example.dto.response;

import lombok.*;
import org.example.model.Item;

import java.util.List;
import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceResponse {
    private String id;
    private String customerName;
    private String customerEmail;
    private List<Item> items;
    private double total;
    private LocalDate createdAt;
}