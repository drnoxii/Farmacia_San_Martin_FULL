package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Dto.PersonaDTO;
import com.sanmartin.farmacias.Services.PersonaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personas")
@Tag(name = "Personas", description = "CRUD de personas del sistema")
public class PersonaController {

    private final PersonaServiceImpl personaService;

    public PersonaController(PersonaServiceImpl personaService) {
        this.personaService = personaService;
    }

    @PostMapping
    @Operation(summary = "Crear persona")
    public ResponseEntity<PersonaDTO> crear(@Valid @RequestBody PersonaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personaService.crear(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener persona por ID")
    public ResponseEntity<PersonaDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar personas")
    public ResponseEntity<List<PersonaDTO>> listar() {
        return ResponseEntity.ok(personaService.listar());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar persona")
    public ResponseEntity<PersonaDTO> actualizar(@PathVariable Long id,
                                                 @Valid @RequestBody PersonaDTO dto) {
        return ResponseEntity.ok(personaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar persona")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada"),
            @ApiResponse(responseCode = "400", description = "Tiene usuario asociado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        personaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}