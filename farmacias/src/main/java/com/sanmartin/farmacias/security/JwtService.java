package com.sanmartin.farmacias.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey claveFirma;
    private final long duracionMs;

    public JwtService(
            @Value("${app.security.jwt.secret}") String claveBase64,
            @Value("${app.security.jwt.expiration-ms}") long duracionMs
    ) {

        byte[] bytesClave = Decoders.BASE64.decode(claveBase64);

        if (bytesClave.length < 32) {
            throw new IllegalArgumentException(
                    "La clave JWT debe tener al menos 256 bits"
            );
        }

        if (duracionMs <= 0) {
            throw new IllegalArgumentException(
                    "La duración del JWT debe ser positiva"
            );
        }

        this.claveFirma = Keys.hmacShaKeyFor(bytesClave);
        this.duracionMs = duracionMs;
    }

    // 1. Generar un token después de autenticar al usuario
    public String generarToken(UserDetails usuario) {

        Instant ahora = Instant.now();

        return Jwts.builder()
                .subject(usuario.getUsername())
                .issuer("farmacia-san-martin")
                .issuedAt(Date.from(ahora))
                .expiration(
                        Date.from(ahora.plusMillis(duracionMs))
                )
                .signWith(claveFirma, Jwts.SIG.HS256)
                .compact();
    }

    // 2. Obtener el correo almacenado en un token válido
    public String extraerCorreo(String token) {
        return extraerClaims(token).getSubject();
    }

    // 3. Verificar si el token pertenece a un usuario válido
    public boolean esTokenValido(
            String token,
            UserDetails usuario
    ) {

        try {
            String correoToken = extraerCorreo(token);

            return correoToken.equals(usuario.getUsername())
                    && usuario.isEnabled()
                    && usuario.isAccountNonLocked()
                    && usuario.isAccountNonExpired()
                    && usuario.isCredentialsNonExpired();

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 4. Verificar firma, emisor y vencimiento
    private Claims extraerClaims(String token) {

        return Jwts.parser()
                .verifyWith(claveFirma)
                .requireIssuer("farmacia-san-martin")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
