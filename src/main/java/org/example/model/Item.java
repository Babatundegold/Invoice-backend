package org.example.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private String name;
    private int quantity;
    private double price;
}