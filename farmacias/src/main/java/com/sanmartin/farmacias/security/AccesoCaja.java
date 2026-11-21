package com.sanmartin.farmacias.security;

import com.sanmartin.farmacias.Repository.CajaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("accesoCaja")
public class AccesoCaja {

    private final CajaRepository cajaRepository;

    public AccesoCaja(CajaRepository cajaRepository) {
        this.cajaRepository = cajaRepository;
    }

    @Transactional(readOnly = true)
    public boolean esPropietario(
            Long idCaja,
            Authentication authentication) {

        if (idCaja == null ||
                authentication == null ||
                !(authentication.getPrincipal()
                        instanceof UsuarioPrincipal principal)) {
            return false;
        }

        Long idUsuario = principal.usuario()
                .getIdUsuario();

        return cajaRepository.findById(idCaja)
                .map(caja -> caja.getUsuario()
                        .getIdUsuario()
                        .equals(idUsuario))
                .orElse(false);
    }
}
