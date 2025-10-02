package com.miapp.gestortareas.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.miapp.gestortareas.DTOs.CreateTareaRequest;
import com.miapp.gestortareas.DTOs.TareaResponse;
import com.miapp.gestortareas.DTOs.UpdateTareaRequest;
import com.miapp.gestortareas.model.Estado;
import com.miapp.gestortareas.model.TareaModel;
import com.miapp.gestortareas.model.UserModel;
import com.miapp.gestortareas.repository.TareaRepository;
import com.miapp.gestortareas.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TareaService {
    private final TareaRepository tareaRepository;
    private final UserRepository userRepository;

    @Transactional
    public TareaResponse crear(String email, CreateTareaRequest req) {
		UserModel userModel = userRepository.findByEmail(email).orElseThrow();
		TareaModel tareaModel = TareaModel.builder()
			.titulo(req.getTitulo())
			.descripcion(req.getDescripcion())
			.estado(Estado.PENDIENTE)
			.prioridad(req.getPrioridad())
			.fechaVencimiento(req.getFechaVencimiento())
			.usuario(userModel).build();
		tareaRepository.save(tareaModel);
		return map(tareaModel);
	}

	public Page<TareaResponse> listar(String email, Pageable pageable) {
		UserModel userModel = userRepository.findByEmail(email).orElseThrow();
		return tareaRepository.findByUsuario(userModel, pageable).map(this::map);
	}

	@Transactional
	public TareaResponse actualizarEstado(String email, Long id, Estado nuevo) {
		UserModel userModel = userRepository.findByEmail(email).orElseThrow();
		TareaModel tareaModel = tareaRepository.findById(id).orElseThrow();
		if (!tareaModel.getUsuario().getId().equals(userModel.getId())) {
			throw new AccessDeniedException("No autorizado para modificar esta tarea");
		}
		tareaModel.setEstado(nuevo);
		tareaModel.setUpdatedAt(Instant.now());
		return map(tareaModel);
	}

	@Transactional
	public TareaResponse actualizar(String email, Long id, UpdateTareaRequest req) {
		UserModel userModel = userRepository.findByEmail(email).orElseThrow();
		TareaModel tareaModel = tareaRepository.findById(id).orElseThrow();

		if (!tareaModel.getUsuario().getId().equals(userModel.getId())) {
			throw new AccessDeniedException("No autorizado para actualizar esta tarea");
		}

		// Actualizamos todos los campos del recurso con los datos del DTO
		tareaModel.setTitulo(req.getTitulo());
		tareaModel.setDescripcion(req.getDescripcion());
		tareaModel.setPrioridad(req.getPrioridad());
		tareaModel.setFechaVencimiento(req.getFechaVencimiento());

		return map(tareaRepository.save(tareaModel));
	}

	public TareaResponse obtenerPorId(String email, Long id) {
		UserModel userModel = userRepository.findByEmail(email).orElseThrow();
		TareaModel tareaModel = tareaRepository.findById(id).orElseThrow();

		// Verifica que el usuario que solicita la tarea sea el dueño
		if (!tareaModel.getUsuario().getId().equals(userModel.getId())) {
			throw new AccessDeniedException("No autorizado para ver esta tarea");
		}

		return map(tareaModel);
	}

	private TareaResponse map(TareaModel t) {
		return TareaResponse.builder()
			.id(t.getId()).titulo(t.getTitulo()).descripcion(t.getDescripcion())
			.estado(t.getEstado()).prioridad(t.getPrioridad())
			.fechaVencimiento(t.getFechaVencimiento())
			.createdAt(t.getCreatedAt())
			.updatedAt(t.getUpdatedAt())
			.build();
	}
    
	@Transactional
	public void eliminar(String email, Long id) {
		UserModel userModel = userRepository.findByEmail(email).orElseThrow();
		TareaModel tareaModel = tareaRepository.findById(id).orElseThrow();

		if (!tareaModel.getUsuario().getId().equals(userModel.getId())) {
			throw new AccessDeniedException("No autorizado para eliminar esta tarea");
		}
		tareaRepository.delete(tareaModel);
	}
}