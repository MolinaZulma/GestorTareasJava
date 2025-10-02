package com.miapp.gestortareas.exception;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;
    private final Instant timestamp = Instant.now();
    private Map<String, String> validationErrors;
}