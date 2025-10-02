package com.miapp.gestortareas.security;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationTime;

    // Generar token
    public String generarToken(String email, String role, Integer tokenVersion) {
        return Jwts.builder()
                .setClaims(Map.of(
                    "role", role,
                    "version", tokenVersion
                )) // Añadimos rol y versión
                .setSubject(email) // el email será el "dueño" del token
                .setIssuedAt(new Date()) // fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // vencimiento
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // algoritmo y clave
                .compact();
    }

    public String generarRefreshToken(String email, Integer tokenVersion) {
        return Jwts.builder()
                .claim("version", tokenVersion)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extraer email (subject)
    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    // Extraer un claim específico
    public <T> T extraerClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosLosClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        if (secretKey == null) {
            throw new IllegalStateException("La clave secreta JWT (jwt.secret) no está configurada en application.properties");
        }
        byte[] keyBytes = this.secretKey.getBytes();
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }
}