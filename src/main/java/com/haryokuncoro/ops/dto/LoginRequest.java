package com.haryokuncoro.ops.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {
    @NotBlank
    @Email
    @Schema(description = "user email", defaultValue = "test@mail.com")
    private String email;

    @NotBlank
    @Schema(description = "user password", defaultValue = "String123!")
    private String password;
}