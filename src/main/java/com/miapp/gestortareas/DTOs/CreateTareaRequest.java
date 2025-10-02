package com.miapp.gestortareas.DTOs;

import java.time.LocalDate;
import com.miapp.gestortareas.model.Prioridad;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTareaRequest {

    @NotBlank(message = "El título no puede estar vacío")
    private String titulo;

    private String descripcion;

    @NotNull(message = "La prioridad no puede ser nula")
    private Prioridad prioridad;

    @FutureOrPresent(message = "La fecha de vencimiento no puede ser en el pasado")
    private LocalDate fechaVencimiento;
}
