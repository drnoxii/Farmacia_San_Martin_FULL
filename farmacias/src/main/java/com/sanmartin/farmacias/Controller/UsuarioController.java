package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Dto.UsuarioDTO;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Rol;
import com.sanmartin.farmacias.Services.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/usuario"})
@Tag(name = "Usuario", description = "CRUD del Usuario")
public class UsuarioController {

    private final IUsuarioService usuarioService;

    public UsuarioController(IUsuarioService usuarioServices) {

        this.usuarioService = usuarioServices;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UsuarioDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public ResponseEntity<List<UsuarioDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/rol/{rol}")
    @Operation(summary = "Listar usuarios por rol")
    public ResponseEntity<List<UsuarioDTO>> listarPorRol(@PathVariable Rol rol) {
        return ResponseEntity.ok(usuarioService.listarPorRol(rol));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar usuarios por estado")
    public ResponseEntity<List<UsuarioDTO>> listarPorEstado(@PathVariable EstadoGeneral estado) {
        return ResponseEntity.ok(usuarioService.listarPorEstado(estado));
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar usuario")
    public ResponseEntity<UsuarioDTO> cambiarEstado(@PathVariable Long id,
                                                    @RequestParam EstadoGeneral estado) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
