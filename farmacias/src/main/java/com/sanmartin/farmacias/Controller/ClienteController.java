package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Dto.ClienteDTO;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Services.IClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Registro y consulta de clientes")
public class ClienteController {

    private final IClienteService clienteService;

    public ClienteController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los clientes")
    public ResponseEntity<List<ClienteDTO>> listarTodo() {
        return ResponseEntity.ok(clienteService.listarTodo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID")
    public ResponseEntity<ClienteDTO> obtener(@PathVariable Long id) {
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con ID: " + id));
    }

    @GetMapping("/dni/{numeroDocumento}")
    @Operation(summary = "Buscar cliente por DNI o RUC (para el flujo de venta)")
    public ResponseEntity<ClienteDTO> buscarPorDocumento(@PathVariable String numeroDocumento) {
        return clienteService.buscarPorDocumento(numeroDocumento)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con documento: " + numeroDocumento));
    }

    @PostMapping
    @Operation(summary = "Registrar cliente (crea Persona + Cliente)")
    public ResponseEntity<ClienteDTO> registrar(@RequestBody @Valid ClienteDTO dto) {
        return new ResponseEntity<>(clienteService.registrar(dto), HttpStatus.CREATED);
    }
}
