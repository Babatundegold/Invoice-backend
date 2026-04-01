package org.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "users")
@Data
public class User {

    @Id
    private String id;

    private String name;
    private String email;
    private String password;

    private String role = "USER"; // USER / ADMIN

    private String plan = "FREE"; // FREE / PREMIUM

    private LocalDate createdAt;
}