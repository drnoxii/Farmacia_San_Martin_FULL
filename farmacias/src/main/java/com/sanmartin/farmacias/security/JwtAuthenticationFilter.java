package com.sanmartin.farmacias.security;


import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Obtener el encabezado Authorization
        String authHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        // 2. Si no contiene Bearer, continuar sin autenticar
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el JWT
        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            // 4. Extraer el correo verificando el JWT
            String correo = jwtService.extraerCorreo(token);

            // 5. Evitar autenticar dos veces
            if (correo != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                // 6. Buscar el usuario actual en Oracle
                UserDetails usuario =
                        userDetailsService.loadUserByUsername(correo);

                // 7. Validar el JWT y el estado del usuario
                if (jwtService.esTokenValido(token, usuario)) {

                    // 8. Crear autenticación con los roles reales
                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    usuario,
                                    null,
                                    usuario.getAuthorities()
                            );

                    autenticacion.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // 9. Guardar la autenticación de esta petición
                    SecurityContextHolder.getContext()
                            .setAuthentication(autenticacion);
                }
            }

        } catch (
                JwtException |
                IllegalArgumentException |
                UsernameNotFoundException e
        ) {
            // Token inválido, expirado o usuario inexistente.
            // La petición continúa sin autenticación.
            SecurityContextHolder.clearContext();
        }

        // 10. Continuar con los demás filtros de Spring
        filterChain.doFilter(request, response);
    }
}
