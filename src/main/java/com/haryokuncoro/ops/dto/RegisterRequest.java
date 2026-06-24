package com.haryokuncoro.ops.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    @Email
    @Schema(description = "user email", defaultValue = "admin@mail.com")
    private String email;

    @NotBlank
    @Schema(description = "user password", defaultValue = "Admin123!")
    private String password;
}