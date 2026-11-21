package com.sanmartin.farmacias.config;

import com.sanmartin.farmacias.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        return http

                // API REST con JWT
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // ======================================
                // AUTORIZACIÓN DE ENDPOINTS
                // ======================================
                .authorizeHttpRequests(auth -> auth

                        // Despacho interno de errores
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR,
                                DispatcherType.FORWARD
                        ).permitAll()

                        // Preflight CORS
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // ==================================
                        // 1. RUTAS PÚBLICAS
                        // ==================================

                        // Login
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        ).permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()


                        // ==================================
                        // 2. AUTENTICACIÓN Y USUARIOS
                        // ==================================

                        // Registrar empleados
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/registro"
                        ).hasRole("ADMIN")


                        // ==================================
                        // 3. PRODUCTOS - CONSULTAS
                        // ==================================

                        // ADMIN y CAJERO consultan
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/producto",
                                "/api/v1/producto/**"
                        ).hasAnyRole("ADMIN", "CAJERO")

                        // POST, PUT y DELETE de productos
                        // quedan para ADMIN por la regla final


                        // ==================================
                        // 4. LOTES
                        // ==================================

                        // Consultar lotes y stock
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/lotes",
                                "/api/lotes/**"
                        ).hasAnyRole("ADMIN", "CAJERO")

                        // Cualquier modificación futura
                        // de lotes queda para ADMIN


                        // ==================================
                        // 5. CLIENTES
                        // ==================================

                        // Listar y buscar clientes
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/clientes",
                                "/api/v1/clientes/**"
                        ).hasAnyRole("ADMIN", "CAJERO")

                        // Registrar nuevos clientes
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/clientes"
                        ).hasAnyRole("ADMIN", "CAJERO")


                        // ==================================
                        // 6. CONSULTA DNI
                        // ==================================

                        // Factiliza para el POS
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/personas/consultar"
                        ).hasAnyRole("ADMIN", "CAJERO")

                        // Resto del CRUD de Personas:
                        // únicamente ADMIN


                        // ==================================
                        // 7. VENTAS
                        // ==================================

                        // Registrar venta
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/ventas"
                        ).hasAnyRole("ADMIN", "CAJERO")

                        // Listado general, consulta por ID
                        // y devoluciones: ADMIN por defecto


                        // ==================================
                        // 8. CAJAS
                        // ==================================

                        // Listado de TODAS las cajas abiertas
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/cajas/abiertas"
                        ).hasRole("ADMIN")

                        // Abrir caja
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/cajas/abrir"
                        ).hasAnyRole("ADMIN", "CAJERO")

                        // Cerrar caja
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/cajas/*/cerrar"
                        ).hasAnyRole("ADMIN", "CAJERO")

                        // Consultar caja abierta de un usuario
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/cajas/usuario/*/abierta",
                                "/api/v1/cajas/usuario/*"
                        ).hasAnyRole("ADMIN", "CAJERO")

                        // Consultar una caja por ID
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/cajas/*"
                        ).hasAnyRole("ADMIN", "CAJERO")

                        // Resumen y movimientos de la caja
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/cajas/*/resumen"
                        ).hasAnyRole("ADMIN", "CAJERO")


                        // ==================================
                        // 9. TODO LO DEMÁS: ADMIN
                        // ==================================

                        // Incluye:
                        // Categorías
                        // Laboratorios
                        // Proveedores
                        // Compras
                        // Usuarios
                        // Reportes
                        // Gestión de personas
                        // Modificaciones de productos
                        // Devoluciones de ventas
                        // Rutas nuevas no declaradas

                        .anyRequest().hasRole("ADMIN")
                )

                // ======================================
                // RESPUESTAS DE SEGURIDAD
                // ======================================
                .exceptionHandling(ex -> ex

                        .authenticationEntryPoint(
                                (request, response, error) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Debes iniciar sesión"
                                        )
                        )

                        .accessDeniedHandler(
                                (request, response, error) ->
                                        response.sendError(
                                                HttpServletResponse.SC_FORBIDDEN,
                                                "No tienes permisos para esta operación"
                                        )
                        )
                )

                // Filtro JWT
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    // Evita registrar el filtro JWT dos veces
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
    jwtFilterRegistration(
            JwtAuthenticationFilter filtro) {

        FilterRegistrationBean<JwtAuthenticationFilter>
                registro = new FilterRegistrationBean<>(filtro);

        registro.setEnabled(false);

        return registro;
    }
}
