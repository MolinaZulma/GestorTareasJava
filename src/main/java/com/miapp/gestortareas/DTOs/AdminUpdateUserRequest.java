package com.miapp.gestortareas.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email es inválido")
    private String email;

    @NotBlank(message = "El rol no puede estar vacío")
    @Pattern(regexp = "ROLE_USER|ROLE_ADMIN", message = "El rol debe ser ROLE_USER o ROLE_ADMIN")
    private String role;
}
