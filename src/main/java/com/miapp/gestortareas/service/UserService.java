package com.miapp.gestortareas.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.miapp.gestortareas.DTOs.AuthResponse;
import com.miapp.gestortareas.DTOs.RegisterRequest;
import com.miapp.gestortareas.DTOs.AdminCreateUserRequest;
import com.miapp.gestortareas.DTOs.AdminUpdateUserRequest;
import com.miapp.gestortareas.DTOs.ChangePasswordRequest;
import com.miapp.gestortareas.DTOs.RefreshTokenRequest;
import com.miapp.gestortareas.DTOs.UserResponse;
import com.miapp.gestortareas.exception.DuplicateResourceException;
import com.miapp.gestortareas.model.UserModel;
import com.miapp.gestortareas.repository.TareaRepository;
import com.miapp.gestortareas.repository.UserRepository;
import com.miapp.gestortareas.security.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
	private final PasswordEncoder encoder;
	private final JwtUtil jwt;
	private final TareaRepository tareaRepository;

	@Transactional
	public AuthResponse register(RegisterRequest req) {
		// El registro público siempre crea usuarios con el rol ROLE_USER.
		if (repo.existsByEmail(req.getEmail())) {
			throw new DuplicateResourceException("El email '" + req.getEmail() + "' ya está registrado.");
		}
		UserModel userModel = UserModel.builder()
			.nombre(req.getNombre())
			.email(req.getEmail())
			.password(encoder.encode(req.getPassword()))
			.role("ROLE_USER")
			.build();
		UserModel savedUser = repo.save(userModel);
		String accessToken = jwt.generarToken(savedUser.getEmail(), savedUser.getRole(), savedUser.getTokenVersion());
		String refreshToken = jwt.generarRefreshToken(savedUser.getEmail(), savedUser.getTokenVersion());
		return new AuthResponse(accessToken, refreshToken, 900L); // 15 minutos
	}

	public AuthResponse login(String email, String password) {
		UserModel userModel = repo.findByEmail(email)
				.orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
		if (!encoder.matches(password, userModel.getPassword())) throw new BadCredentialsException("Credenciales inválidas");
		String accessToken = jwt.generarToken(userModel.getEmail(), userModel.getRole(), userModel.getTokenVersion());
		String refreshToken = jwt.generarRefreshToken(userModel.getEmail(), userModel.getTokenVersion());
		return new AuthResponse(accessToken, refreshToken, 900L);
	}

	public UserResponse me(String email) {
		UserModel user = repo.findByEmail(email).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado."));
		return mapToUserResponse(user);
	}

	/**
	 * Método para ser usado por un administrador para crear un nuevo usuario con un rol específico. Devuelve un DTO.
	 */
	@Transactional
	public UserResponse createUser(AdminCreateUserRequest req) {
		if (repo.existsByEmail(req.getEmail())) {
			throw new DuplicateResourceException("El email '" + req.getEmail() + "' ya está en uso.");
		}
		UserModel userModel = UserModel.builder()
			.nombre(req.getNombre())
			.email(req.getEmail())
			.password(encoder.encode(req.getPassword()))
			.role(req.getRole())
			.build();
		UserModel savedUser = repo.save(userModel);
		return mapToUserResponse(savedUser);
	}

	public List<UserResponse> findAll() {
		return repo.findAll()
				.stream()
				.map(this::mapToUserResponse)
				.collect(Collectors.toList());
	}

	private UserResponse mapToUserResponse(UserModel user) {
		return UserResponse.builder()
				.id(user.getId())
				.nombre(user.getNombre())
				.email(user.getEmail())
				.role(user.getRole())
				.build();
	}

	@Transactional
	public void deleteUser(Long id) {
		UserModel user = repo.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Usuario con ID " + id + " no encontrado."));

		// Eliminar todas las tareas asociadas al usuario
		tareaRepository.deleteByUsuario(user);

		// Eliminar el usuario
		repo.delete(user);
	}

	@Transactional
	public UserResponse updateUser(Long id, AdminUpdateUserRequest request) {
		UserModel user = repo.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Usuario con ID " + id + " no encontrado."));

		// Si el email ha cambiado, verificar que el nuevo no esté ya en uso por otro usuario.
		if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
			if (repo.existsByEmail(request.getEmail())) {
				throw new DuplicateResourceException("El email '" + request.getEmail() + "' ya está en uso por otro usuario.");
			}
			user.setEmail(request.getEmail());
		}

		// Actualizar los demás campos
		user.setNombre(request.getNombre());
		user.setRole(request.getRole());

		UserModel updatedUser = repo.save(user);
		return mapToUserResponse(updatedUser);
	}

	@Transactional
	public void changePassword(String email, ChangePasswordRequest request) {
		if (!request.getNewPassword().equals(request.getConfirmPassword())) {
			throw new IllegalArgumentException("La nueva contraseña y la confirmación no coinciden.");
		}

		UserModel user = repo.findByEmail(email)
				.orElseThrow(() -> new NoSuchElementException("Usuario no encontrado.")); // No debería ocurrir si está autenticado

		// Verificar la contraseña actual
		if (!encoder.matches(request.getCurrentPassword(), user.getPassword())) {
			throw new BadCredentialsException("La contraseña actual es incorrecta.");
		}

		user.setPassword(encoder.encode(request.getNewPassword()));
		repo.save(user);
	}

	@Transactional
	public AuthResponse refreshToken(RefreshTokenRequest request) {
		String refreshToken = request.getRefreshToken();
		String email = jwt.extraerEmail(refreshToken);
		Integer tokenVersion = jwt.extraerClaim(refreshToken, claims -> claims.get("version", Integer.class));

		UserModel user = repo.findByEmail(email).orElseThrow(() -> new NoSuchElementException("Usuario del token no encontrado"));

		// Verificar que la versión del refresh token es la actual
		if (tokenVersion == null || user.getTokenVersion() != tokenVersion) {
			throw new BadCredentialsException("Refresh token inválido o revocado.");
		}

		// Incrementar la versión del token para invalidar todos los tokens antiguos
		user.setTokenVersion(user.getTokenVersion() + 1);
		repo.save(user);

		String newAccessToken = jwt.generarToken(user.getEmail(), user.getRole(), user.getTokenVersion());
		String newRefreshToken = jwt.generarRefreshToken(user.getEmail(), user.getTokenVersion());

		return new AuthResponse(newAccessToken, newRefreshToken, 900L);
	}
}
