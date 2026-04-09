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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String id;

    private String name;
    private String email;
    private String password;

    @Builder.Default
    private String role = "USER";   // Now the builder will use "USER" by default

    @Builder.Default
    private String plan = "FREE";   // Now the builder will use "FREE" by default

    private LocalDate createdAt;
}