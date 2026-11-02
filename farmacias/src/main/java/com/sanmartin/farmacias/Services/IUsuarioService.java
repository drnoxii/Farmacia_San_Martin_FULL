
package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.UsuarioDTO;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Rol;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IUsuarioService {

    UsuarioDTO obtenerPorId(Long id);

    List<UsuarioDTO> listar();

    List<UsuarioDTO> listarPorRol(Rol rol);

    List<UsuarioDTO> listarPorEstado(EstadoGeneral estado);

    UsuarioDTO cambiarEstado(Long id, EstadoGeneral estado);

    UsuarioDTO actualizar(Long id, UsuarioDTO datos);

    void eliminar(Long id);
}
