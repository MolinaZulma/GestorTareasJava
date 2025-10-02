package com.miapp.gestortareas.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miapp.gestortareas.DTOs.CreateTareaRequest;
import com.miapp.gestortareas.DTOs.TareaResponse;
import com.miapp.gestortareas.DTOs.UpdateTareaRequest;
import com.miapp.gestortareas.model.Estado;
import com.miapp.gestortareas.service.TareaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tareas")
@RequiredArgsConstructor
public class TareaController {

	private final TareaService tareaService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

	 // ---------- LISTAR TAREAS ----------
    @GetMapping
    public ResponseEntity<Page<TareaResponse>> listarTareas(@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        String email = getCurrentUserEmail();
        Page<TareaResponse> tareas = tareaService.listar(email, pageable);
        return ResponseEntity.ok(tareas);
    }

    // ---------- OBTENER UNA TAREA POR ID ----------
    @GetMapping("/{id}")
    public ResponseEntity<TareaResponse> obtenerTareaPorId(@PathVariable Long id) {
        String email = getCurrentUserEmail();
        TareaResponse tarea = tareaService.obtenerPorId(email, id);
        return ResponseEntity.ok(tarea);
    }

    // ---------- CREAR TAREA ----------
    @PostMapping
    @Operation(summary = "Crea una nueva tarea para el usuario autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tarea creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado si el token no es válido")
    })
    public ResponseEntity<TareaResponse> crearTarea(@Valid @RequestBody CreateTareaRequest request) {
        String email = getCurrentUserEmail();
        TareaResponse tareaCreada = tareaService.crear(email, request);
        return new ResponseEntity<>(tareaCreada, HttpStatus.CREATED);
    }

    // ---------- ACTUALIZAR ESTADO DE TAREA ----------
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TareaResponse> actualizarEstadoTarea(
            @PathVariable Long id,
            @NotNull @RequestBody Estado nuevoEstado) {
        String email = getCurrentUserEmail();
        TareaResponse tareaActualizada = tareaService.actualizarEstado(email, id, nuevoEstado);
        return ResponseEntity.ok(tareaActualizada);
    }

    // ---------- ACTUALIZACIÓN COMPLETA DE TAREA ----------
    @PutMapping("/{id}")
    public ResponseEntity<TareaResponse> actualizarTarea(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTareaRequest request) {
        String email = getCurrentUserEmail();
        TareaResponse tareaActualizada = tareaService.actualizar(email, id, request);
        return ResponseEntity.ok(tareaActualizada);
    }

    // ---------- ELIMINAR TAREA ----------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        String email = getCurrentUserEmail();
        tareaService.eliminar(email, id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content es la respuesta estándar para un DELETE exitoso.
    }

}
