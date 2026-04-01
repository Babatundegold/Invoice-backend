package org.example.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "invoices")
public class Invoice {

    @Id
    private String id;

    private String customerName;
    private String customerEmail;

    private List<Item> items;

    private double total;

    private LocalDate createdAt;
}
