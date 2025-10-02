package com.miapp.gestortareas.security;


import com.miapp.gestortareas.model.UserModel;
import com.miapp.gestortareas.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX.length());
            String email = jwtUtil.extraerEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Cargar usuario para verificar la versión del token
                UserModel user = userRepository.findByEmail(email).orElse(null);
                Integer tokenVersion = jwtUtil.extraerClaim(token, claims -> claims.get("version", Integer.class));

                // Solo si el usuario existe y la versión del token es la correcta
                if (user != null && tokenVersion != null && user.getTokenVersion() == tokenVersion) {
                    String role = jwtUtil.extraerClaim(token, claims -> claims.get("role", String.class));
                    if (role != null) {
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(authority));
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}