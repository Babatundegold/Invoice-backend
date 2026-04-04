package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "users")
@Data
@Builder // This creates the .builder() method
@AllArgsConstructor // Required by @Builder
@NoArgsConstructor
public class User {

    @Id
    private String id;

    private String name;
    private String email;
    private String password;

    private String role = "USER";   // USER / ADMIN
    private String plan = "FREE";   // FREE / PREMIUM

    private LocalDate createdAt;
}

