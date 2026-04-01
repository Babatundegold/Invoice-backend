package org.example.dto.request;


import lombok.Data;
import org.example.model.Item;

import java.util.List;

@Data
public class InvoiceRequest {
    private String customerName;
    private String customerEmail;
    private List<Item> items;
}