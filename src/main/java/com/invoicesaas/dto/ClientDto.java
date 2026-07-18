package com.invoicesaas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientDto {
    private Long id;
    @NotBlank
    private String name;
    private String email;
    private String phone;
    private String address;
}
