package com.miapp.gestortareas.DTOs;

import java.time.Instant;
import java.time.LocalDate;

import com.miapp.gestortareas.model.Estado;
import com.miapp.gestortareas.model.Prioridad;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TareaResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private Estado estado;
    private Prioridad prioridad;
    private LocalDate fechaVencimiento;
    private Instant createdAt;
    private Instant updatedAt;
}
