package com.miapp.gestortareas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miapp.gestortareas.DTOs.AuthResponse;
import com.miapp.gestortareas.DTOs.LoginRequest;
import com.miapp.gestortareas.DTOs.RefreshTokenRequest;
import com.miapp.gestortareas.DTOs.RegisterRequest;
import com.miapp.gestortareas.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth") // todas las rutas comienzan con /auth
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;

    // ---------- REGISTRO ----------
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ---------- LOGIN ----------
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = userService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
