package com.sanmartin.farmacias.security;

import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UsuarioPrincipal(Usuario usuario) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + usuario.getRol().name()
                )
        );
    }

    // Contraseña BCrypt almacenada en Oracle
    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    // En nuestra farmacia el identificador es el CORREO
    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Solo permitir usuarios ACTIVOS
    @Override
    public boolean isEnabled() {
        return usuario.getEstadoGeneral() == EstadoGeneral.ACTIVO;
    }

}
